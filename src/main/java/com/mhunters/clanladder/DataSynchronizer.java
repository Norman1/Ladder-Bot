package com.mhunters.clanladder;

import com.mhunters.clanladder.data.GameHistory;
import com.mhunters.clanladder.data.Player;
import com.mhunters.clanladder.data.Template;
import com.mhunters.clanladder.data.warzone.GameDeletionRequest;
import com.mhunters.clanladder.data.warzone.GameQueryResponse;
import com.mhunters.clanladder.elo.EloUpdater;
import com.mhunters.clanladder.external.FileSystemAccess;
import com.mhunters.clanladder.external.WarzoneAccess;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class is responsible for giving access to the saved data and keeping it in sync.
 * No other class is allowed to contain any information regarding the way the data is stored.
 */
@Service
@Slf4j
public class DataSynchronizer {

    private static final int MAX_DAYS_IN_LOBBY = 2;

    @Getter
    private List<Template> allTemplates;

    @Getter
    private List<Player> allPlayers;

    // games which are neither completed nor dead. Includes games still in the lobby.
    @Getter
    private List<GameHistory> ongoingGames;

    // Games which were either declined by at least one player or which was not accepted by both players for too long
    @Getter
    private List<GameHistory> deadOrOutdatedGames;

    // Games which were not completed before the sync but now are
    @Getter
    private List<GameHistory> newlyFinishedGames;

    @Getter
    @Setter
    private List<GameHistory> newlyCreatedGames;

    @Autowired
    private FileSystemAccess fileSystemAccess;

    @Autowired
    private WarzoneAccess warzoneAccess;

    @Autowired
    private EloUpdater eloUpdater;

    @Autowired
    private GoogleSheetSynchronization googleSheetSynchronization;

    @Value("${hostEmail}")
    private String hostEmail;
    @Value("${hostApiToken}")
    private String hostApiToken;

    private enum GameState {
        LOBBY_OK, LOBBY_DEAD_OR_OUTDATED, ONGOING, FINISHED, NOT_FINDABLE
    }


    /**
     * Loads the data from the file system and synchronizes potentially outdated data by calling the Warzone API to get the new game states.
     * This function does not write data back into the file system.
     */
    public void loadStartState() {
        /*
         * Step 1: Synchronize the file system data with Google Sheets.
         * Step 2: Load the templates from the file system.
         * Step 3: Load the games from the file system. Check the state of the ongoing games and update the information accordingly.
         * Step 4: Load the players from the file system and inject the information regarding their ongoing games.
         * Step 5: Update the players Elo ratings.
         */
        googleSheetSynchronization.synchronizeFileSystemData();
        synchronizeTemplates();
        synchronizeGames();
        synchronizePlayers();
        updateEloRatings();
    }


    /**
     * Writes the new state after everything is done back to the file system and updates the Google Sheet accordingly.
     */
    public void updateAtEnd() {
        /*
         * 1:
         * - Tell Warzone to delete all dead games.
         * - Collect all non dead games and write them to the file system. We do not write historic games currently.
         * - Write the players back to the file system.
         * - Write the templates back to the file system.
         * 2:
         * - Update the Google Sheet rankings
         */
        deleteDeadGames();
        writeGamesBackToFileSystem();
        writePlayersBackToFileSystem();

        googleSheetSynchronization.synchronizeGoogleSheet();
    }


    // Step 1
    private void synchronizeTemplates() {
        allTemplates = fileSystemAccess.loadTemplates();
    }

    // Step 2
    private void synchronizeGames() {
        List<GameHistory> allGames = fileSystemAccess.loadGames();
        List<GameHistory> nonHistoricGames = allGames.stream().filter(g -> !"Finished".equals(g.getState())).collect(Collectors.toList());

        deadOrOutdatedGames = new ArrayList<>();
        ongoingGames = new ArrayList<>();
        newlyFinishedGames = new ArrayList<>();
        // {ongoing games saved} + Warzone access --> {ongoing games, dead games, newly finished games}
        for (GameHistory gameHistory : nonHistoricGames) {
            GameHistory warzoneUpdatedGameHistory = queryWarzoneGame(gameHistory.getGameId());
            GameState gameState = getGameState(warzoneUpdatedGameHistory);
            // We do not handle that game so it will just automatically disappear after writing the ongoing games back.
            if (gameState == GameState.NOT_FINDABLE) {
                continue;
            }
            if (gameState == GameState.LOBBY_OK || gameState == GameState.ONGOING) {
                ongoingGames.add(warzoneUpdatedGameHistory);
            } else if (gameState == GameState.FINISHED) {
                newlyFinishedGames.add(warzoneUpdatedGameHistory);
            } else if (gameState == GameState.LOBBY_DEAD_OR_OUTDATED) {
                deadOrOutdatedGames.add(warzoneUpdatedGameHistory);
            } else {
                LocalDateTime creationDate = warzoneUpdatedGameHistory.getCreationDate();
                if (creationDate.isBefore(LocalDateTime.now().minusDays(MAX_DAYS_IN_LOBBY))) {
                    deadOrOutdatedGames.add(warzoneUpdatedGameHistory);
                } else {
                    ongoingGames.add(warzoneUpdatedGameHistory);
                }
            }
        }
    }

    private GameState getGameState(GameHistory gameHistory) {
        String gameState = gameHistory.getState();
        if (gameState == null) {
            return GameState.NOT_FINDABLE;
        }
        if (gameState.equals("Finished")) {
            return GameState.FINISHED;
        }
        if (gameState.equals("Declined")) {
            return GameState.LOBBY_DEAD_OR_OUTDATED;
        }
        if (gameState.equals("Playing") || gameState.equals("DistributingTerritories")) {
            return GameState.ONGOING;
        }
        if (gameState.equals("WaitingForPlayers")) {
            String p1State = gameHistory.getP1State();
            String p2State = gameHistory.getP2State();
            if (p1State.equals("Declined") || p2State.equals("Declined")) {
                return GameState.LOBBY_DEAD_OR_OUTDATED;
            }
        }
        return null;
    }

    private GameHistory queryWarzoneGame(int gameId) {
        GameQueryResponse gameQueryResponse = warzoneAccess.readGame(gameId, hostEmail, hostApiToken);
        // state = null when the game is deleted
        GameHistory gameHistory = new GameHistory();
        gameHistory.setGameId(gameId);
        if (gameQueryResponse.getState() == null) {
            return gameHistory;
        }
        gameHistory.setState(gameQueryResponse.getState());
        gameHistory.setCreationDate(DateUtils.parseDate(gameQueryResponse.getCreated()));
        List<GameQueryResponse.GamePlayerQueryResponse> players = gameQueryResponse.getPlayers();
        gameHistory.setP1Token(players.get(0).getId());
        gameHistory.setP1State(players.get(0).getState());
        gameHistory.setP2Token(players.get(1).getId());
        gameHistory.setP2State(players.get(1).getState());
        return gameHistory;
    }


    // Step 3
    private void synchronizePlayers() {
        allPlayers = fileSystemAccess.loadPlayers();

        for (GameHistory ongoingGame : ongoingGames) {
            Optional<Player> player1Optional = allPlayers.stream().filter(p -> ongoingGame.getP1Token().equals(p.getInviteToken())).findAny();
            Optional<Player> player2Optional = allPlayers.stream().filter(p -> ongoingGame.getP2Token().equals(p.getInviteToken())).findAny();
            List<Optional<Player>> playersOptional = List.of(player1Optional, player2Optional);
            for (Optional<Player> playerOptional : playersOptional) {
                if (playerOptional.isPresent()) {
                    Player player = playerOptional.get();
                    player.setCurrentGameCount(player.getCurrentGameCount() + 1);
                } else {
                    log.debug("Found non existent player in the following game: " + ongoingGame);
                }
            }
        }
    }


    // Step 4
    private void updateEloRatings() {
        eloUpdater.updateEloRankings();
    }

    private void deleteDeadGames() {
        for (GameHistory deadGame : deadOrOutdatedGames) {
            GameDeletionRequest gdr = new GameDeletionRequest(hostEmail, hostApiToken, deadGame.getGameId());
            warzoneAccess.deleteGame(gdr);
        }
    }

    private void writeGamesBackToFileSystem() {
        List<GameHistory> gamesToKeepSaved = new ArrayList<>();
        gamesToKeepSaved.addAll(ongoingGames);
        gamesToKeepSaved.addAll(newlyCreatedGames);
        fileSystemAccess.replaceGames(gamesToKeepSaved);
    }

    private void writePlayersBackToFileSystem() {
        fileSystemAccess.replacePlayers(allPlayers);
    }


}
