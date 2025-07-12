package com.mhunters.clanladder;

import com.mhunters.clanladder.data.GameHistory;
import com.mhunters.clanladder.data.Player;
import com.mhunters.clanladder.data.Template;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetGame;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetRanking;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetSignup;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetTemplate;
import com.mhunters.clanladder.external.FileSystemAccess;
import com.mhunters.clanladder.external.GoogleSheetAccess;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
/**
 * This class is responsible for synchronization between the Google Sheet and the file system.
 */
public class GoogleSheetSynchronization {

    private static final int INITIAL_ELO = 1000;
    private static final int MAX_GAMES = 5;

    @Value("${googleSheetTemplateLoadingEnabled}")
    private boolean googleSheetTemplateLoadingEnabled;

    @Value("${googleSheetSignupSynchronizationEnabled}")
    private boolean googleSheetSignupSynchronizationEnabled;

    @Value("${googleSheetRankingSynchronizationEnabled}")
    private boolean googleSheetRankingSynchronizationEnabled;

    @Value("${googleSheetGameInsertEnabled}")
    private boolean googleSheetGameInsertEnabled;

    @Autowired
    private GoogleSheetAccess googleSheetAccess;

    @Autowired
    private FileSystemAccess fileSystemAccess;

    @Autowired
    private DataSynchronizer dataSynchronizer;

    /**
     * Fetches data from Google Sheets and writes it to the file system.
     * - Google Sheet templates --> file system templates
     * - Google Sheet players --> file system players
     */
    public void synchronizeFileSystemData() {
        try {
            synchronizeTemplates();
            synchronizeSignup();
        } catch (Exception ex) {
            log.error("synchronizeFileSystemData failed", ex);
        }
    }

    /**
     * Updates the Google Sheets.
     * - File system rankings --> Google Sheet rankings
     * - Insert the newly completed games into the Google Sheet
     */
    public void synchronizeGoogleSheet() {
        try {
            synchronizeRankings();
            insertNewlyCompletedGames();
        } catch (Exception ex) {
            log.error("synchronizeGoogleSheet failed", ex);
        }

    }

    @SneakyThrows
    private void synchronizeTemplates() {
        if (!googleSheetTemplateLoadingEnabled) {
            return;
        }
        List<GoogleSheetTemplate> sheetTemplates = googleSheetAccess.readTemplates();
        List<Template> fileSystemTemplates = new ArrayList<>();
        for (GoogleSheetTemplate sheetTemplate : sheetTemplates) {
            Template template = new Template();
            template.setName(sheetTemplate.getName());
            template.setLink(sheetTemplate.getLink());
            fileSystemTemplates.add(template);
        }
        fileSystemAccess.replaceTemplates(fileSystemTemplates);

    }

    @SneakyThrows
    private void synchronizeSignup() {
        if (!googleSheetSignupSynchronizationEnabled) {
            return;
        }
        /*
         * - Player present in file system + sheet --> update max games
         * - Player present in sheet but not in file system --> insert player
         * - Player missing in sheet but present in file system --> delete player by not writing him back
         */
        List<GoogleSheetSignup> googleSheetSignups = googleSheetAccess.readSignups();
        List<Player> fileSystemPlayers = fileSystemAccess.loadPlayers();
        List<Player> updatedPlayers = new ArrayList<>();

        for (GoogleSheetSignup googleSheetSignup : googleSheetSignups) {
            googleSheetSignup.setMaxGames(Math.max(0, Math.min(googleSheetSignup.getMaxGames(), MAX_GAMES)));
            Optional<Player> fileSystemPlayerOptional = fileSystemPlayers.stream().filter(fsp -> fsp.getInviteToken().equals(googleSheetSignup.getToken())).findAny();
            if (fileSystemPlayerOptional.isPresent()) {
                Player fileSystemPlayer = fileSystemPlayerOptional.get();
                fileSystemPlayer.setMaxGames(googleSheetSignup.getMaxGames());
                updatedPlayers.add(fileSystemPlayer);
            } else {
                Player newPlayer = new Player(googleSheetSignup.getName(), googleSheetSignup.getToken(),
                        googleSheetSignup.getMaxGames(),
                        INITIAL_ELO, 0, false);
                updatedPlayers.add(newPlayer);
            }
        }
        fileSystemAccess.replacePlayers(updatedPlayers);
    }


    @SneakyThrows
    private void synchronizeRankings() {
        if (!googleSheetRankingSynchronizationEnabled) {
            return;
        }
        /*
         * Step 1: Delete the old rankings
         * Step 2: Insert the new rankings
         */

        googleSheetAccess.deleteRankings();

        List<Player> players = fileSystemAccess.loadPlayers();
        // only display players playing currently
        players = players.stream().filter(p -> p.getMaxGames() > 0).collect(Collectors.toList());
        Collections.sort(players, (p1, p2) -> p2.getElo() - p1.getElo());
        List<GoogleSheetRanking> googleSheetRankings = new ArrayList<>();
        for (Player player : players) {
            int rank = players.indexOf(player) + 1;
            GoogleSheetRanking googleSheetRanking = new GoogleSheetRanking();
            googleSheetRanking.setRank(rank);
            googleSheetRanking.setName(player.getName());
            googleSheetRanking.setRating(player.getElo());
            googleSheetRankings.add(googleSheetRanking);
        }
        googleSheetAccess.insertRankings(googleSheetRankings);
    }

    @SneakyThrows
    private void insertNewlyCompletedGames() {
        if (!googleSheetGameInsertEnabled) {
            return;
        }

        List<GameHistory> newlyFinishedGames = dataSynchronizer.getNewlyFinishedGames();
        List<GameHistory> deadOrOutdatedGames = dataSynchronizer.getDeadOrOutdatedGames();
        List<GameHistory> allConsideredGames = new ArrayList<>();
        allConsideredGames.addAll(newlyFinishedGames);
        allConsideredGames.addAll(deadOrOutdatedGames);

        // remove games which do not count due to one player having left the clan
        allConsideredGames.removeIf(game -> game.getSheetReportInfo().getResult() == null);

        allConsideredGames.sort(Comparator.comparing(GameHistory::getLastTurnDate));
        List<GoogleSheetGame> googleSheetGames = new ArrayList<>();
        for (GameHistory gameHistory : allConsideredGames) {
            GoogleSheetGame gss = new GoogleSheetGame();
            gss.setReportDate(LocalDate.now().toString());
            gss.setTemplate(getTemplateName(gameHistory.getTemplateId()));
            gss.setPlayer1Name(gameHistory.getSheetReportInfo().getPlayer1Name());
            gss.setPlayer2Name(gameHistory.getSheetReportInfo().getPlayer2Name());
            gss.setResult(gameHistory.getSheetReportInfo().getResult());
            gss.setLink(getGameLinkColumn(gameHistory, deadOrOutdatedGames));
            googleSheetGames.add(gss);
        }

        if (googleSheetGames.size() > 0) {
            googleSheetAccess.insertGameHistory(googleSheetGames);
        }
    }

    // "Link" column in the Google sheet, containing the game link or an explanation why there is none.
    private String getGameLinkColumn(GameHistory gameHistory, List<GameHistory> deadOrOutdatedGames) {

        if (!deadOrOutdatedGames.contains(gameHistory)) {
            return "https://www.warzone.com/MultiPlayer?GameID=" + gameHistory.getGameId();
        }
        String player1Name = gameHistory.getSheetReportInfo().getPlayer1Name();
        String player2Name = gameHistory.getSheetReportInfo().getPlayer2Name();

        String p1State = gameHistory.getP1State();
        String p2State = gameHistory.getP2State();

        String p1Message = getPlayerMessage(player1Name, p1State);
        String p2Message = getPlayerMessage(player2Name, p2State);


        String outMessage = "Deleted game (";
        outMessage += p1Message;
        if (!p1Message.isEmpty() && !p2Message.isEmpty()) {
            outMessage += " and ";
        }
        outMessage += p2Message;
        outMessage += ")";


        return outMessage;
    }

    private String getPlayerMessage(String playerName, String playerState) {
        boolean playerFailedToJoin = playerState.equals("Invited") || playerState.equals("Declined");
        if (!playerFailedToJoin) {
            return "";
        }
        String out = playerName;
        if (playerState.equals("Invited")) {
            out += " did not accept the invite";
        } else if (playerState.equals("Declined")) {
            out += " declined the invite";
        }
        return out;
    }


    private String getTemplateName(int templateId) {
        List<Template> templates = dataSynchronizer.getAllTemplates();
        Optional<Template> templateOptional = templates.stream().filter(t -> t.getId() == templateId).findAny();
        if (templateOptional.isPresent()) {
            return templateOptional.get().getName();
        }
        return "Unknown";
    }


}
