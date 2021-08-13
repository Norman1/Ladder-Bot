package com.mhunters.clanladder;

import com.mhunters.clanladder.data.Player;
import com.mhunters.clanladder.data.Template;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetRanking;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetSignup;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetTemplate;
import com.mhunters.clanladder.external.FileSystemAccess;
import com.mhunters.clanladder.external.GoogleSheetAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
/**
 * This class is responsible for synchronization between the Google Sheet and the file system.
 */
public class GoogleSheetSynchronization {

    private static final int INITIAL_ELO = 1000;
    private static final int MAX_GAMES = 3;

    @Value("${googleSheetTemplateLoadingEnabled}")
    private boolean googleSheetTemplateLoadingEnabled;

    @Value("${googleSheetSignupSynchronizationEnabled}")
    private boolean googleSheetSignupSynchronizationEnabled;

    @Value("${googleSheetRankingSynchronizationEnabled}")
    private boolean googleSheetRankingSynchronizationEnabled;

    @Autowired
    private GoogleSheetAccess googleSheetAccess;

    @Autowired
    private FileSystemAccess fileSystemAccess;

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
     * Fetches data from the file system and updates the Google Sheets.
     * - File system rankings --> Google Sheet rankings
     */
    public void synchronizeGoogleSheet() {
        try {
            synchronizeRankings();
        } catch (Exception ex) {
            log.error("synchronizeGoogleSheet failed", ex);
        }

    }

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
                        INITIAL_ELO, 0, 0);
                updatedPlayers.add(newPlayer);
            }
        }
        fileSystemAccess.replacePlayers(updatedPlayers);
    }


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


}
