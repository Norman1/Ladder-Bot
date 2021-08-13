package com.mhunters.clanladder;

import com.mhunters.clanladder.data.*;
import com.mhunters.clanladder.elo.EloUpdater;
import com.mhunters.clanladder.external.FileSystemAccess;
import com.mhunters.clanladder.external.WarzoneAccess;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for giving access to the saved data and keeping it in sync.
 * No other class is allowed to contain any information regarding the way the data is stored.
 */
@Service
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

    @Value("${hostEmail}")
    private String hostEmail;
    @Value("${hostApiToken}")
    private String hostApiToken;

    private enum GameState {
        LOBBY_OK, LOBBY_DEAD_OR_OUTDATED, ONGOING, FINISHED
    }


    /**
     * Writes the new state after everything is done back to the file system and updates the Google Sheet accordingly.
     */
    public void updateAtEnd() {
        /*
         * - Tell Warzone to delete all dead games.
         * - Collect all non dead games and write them to the file system. We do not write historic games currently.
         * - Write the players back to the file system.
         * - Write the templates back to the file system.
         */
        deleteDeadGames();
        writeGamesBackToFileSystem();
        writePlayersBackToFileSystem();
        writeTemplatesBackToFileSystem();
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

    private void writeTemplatesBackToFileSystem() {
        // not used yet
    }


    /**
     * Loads the data from the file system and synchronizes potentially outdated data by calling the Warzone API to get the new game states.
     * This function does not write data back into the file system.
     */
    public void loadStartState() {
        /*
         * Step 1: Load the templates from the file system. Might or might not call the Google Sheet to synchronize.
         * Step 2: Load the games from the file system. Check the state of the ongoing games and update the information accordingly.
         * Step 3: Load the players from the file system and inject the information regarding their ongoing games.
         * Step 4: Update the players Elo ratings
         */
        synchronizeTemplates();
        synchronizeGames();
        synchronizePlayers();
        updateEloRatings();
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
        GameHistory gameHistory = new GameHistory();
        gameHistory.setGameId(gameId);
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
            Player player1 = allPlayers.stream().filter(p -> ongoingGame.getP1Token().equals(p.getInviteToken())).findAny().get();
            Player player2 = allPlayers.stream().filter(p -> ongoingGame.getP2Token().equals(p.getInviteToken())).findAny().get();
            player1.setCurrentGameCount(player1.getCurrentGameCount() + 1);
            player2.setCurrentGameCount(player2.getCurrentGameCount() + 1);
        }
    }

    // Step 4
    private void updateEloRatings() {
        eloUpdater.updateEloRankings();
    }

}
