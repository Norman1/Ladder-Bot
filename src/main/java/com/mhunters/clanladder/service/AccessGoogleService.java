package com.mhunters.clanladder.service;

import com.mhunters.clanladder.data.DataWrapper;
import com.mhunters.clanladder.data.LadderRanking;
import com.mhunters.clanladder.data.Player;
import com.mhunters.clanladder.data.Template;
import com.mhunters.clanladder.data.warzone.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Merely reads the initial state from the Google sheet without any logic.
 */
@Service
public class AccessGoogleService {

    private final Logger logger = Logger.getLogger("AccessGoogleService");

    @Autowired
    private GoogleSheetsService sheetsService;

    @Value("${google.sheet.id}")
    private String spreadsheetId;


    public void reaadAllData(DataWrapper dataWrapper) {
        logger.info("Called AccessGoogleService.readAllData");
        readTemplates(dataWrapper);
        readPlayers(dataWrapper);
        readOngoingGames(dataWrapper);
        readLadder(dataWrapper);
    }

    public void writeAllData(DataWrapper dataWrapper) {
        logger.info("Called AccessGoogleService.writeAllData");
        writeLadder(dataWrapper);

    }

    private void writeLadder(DataWrapper dataWrapper) {
        List<LadderRanking> ladderRankings = dataWrapper.getLadderRankings();
        List<List<Object>> rows = new ArrayList<>();
        for (LadderRanking ladderRanking : ladderRankings) {
            List<Object> row = new ArrayList<>();
            row.add(ladderRanking.getRank());
            row.add(ladderRanking.getName());
            row.add(ladderRanking.getRating());
            row.add(ladderRanking.getPlayerId());
            rows.add(row);
        }
        try {
            sheetsService.clearSheet(spreadsheetId, "ladder!A2:D");
            sheetsService.writeToSheet(spreadsheetId, "ladder!A2:D", rows);
        } catch (Exception e) {
            logger.severe("Failed to write ladder data: " + e.getMessage());
        }
    }

    private void readTemplates(DataWrapper dataWrapper) {
        List<List<Object>> rows = sheetsService.readSheet(spreadsheetId, "templates!A2:B101");
        List<Template> templates = new ArrayList<>();
        dataWrapper.setTemplates(templates);
        for (List<Object> row : rows) {
            String name = row.get(0).toString();
            String link = row.size() > 1 ? row.get(1).toString() : "";
            Template template = new Template();
            template.setName(name);
            template.setLink(link);
            int id = Integer.parseInt(template.getLink().split("=")[1]);
            template.setId(id);
            templates.add(template);
        }
    }

    private void readPlayers(DataWrapper dataWrapper) {
        List<List<Object>> rows = sheetsService.readSheet(spreadsheetId, "signup!A2:C101");
        List<Player> players = new ArrayList<>();
        dataWrapper.setPlayers(players);
        for (List<Object> row : rows) {
            String name = row.get(0).toString();
            String token = row.get(1).toString();
            int maxGames = Integer.parseInt(row.get(2).toString());
            Player player = new Player();
            player.setName(name);
            player.setInviteToken(token);
            player.setMaxGames(maxGames);
            players.add(player);
        }
    }

    private void readOngoingGames(DataWrapper dataWrapper) {
        List<List<Object>> rows = sheetsService.readSheet(spreadsheetId, "'Ongoing Games'!A2:D101");
        List<Game> games = new ArrayList<>();
        dataWrapper.setGames(games);
        for (List<Object> row : rows) {
            Game game = new Game();
            games.add(game);
            String link = row.get(0).toString();
            String player1Id = row.get(1).toString();
            String player2Id = row.get(2).toString();
            String status = row.get(3).toString();
            game.setLink(link);
            game.setPlayer1Token(player1Id);
            game.setPlayer2Token(player2Id);
            game.setStatus(status);
        }
    }

    private void readLadder(DataWrapper dataWrapper) {
        List<List<Object>> rows = sheetsService.readSheet(spreadsheetId, "ladder!A2:D101");
        List<LadderRanking> ladderRankings = new ArrayList<>();
        dataWrapper.setLadderRankings(ladderRankings);
        for (List<Object> row : rows) {
            LadderRanking ladderRanking = new LadderRanking();
            ladderRankings.add(ladderRanking);
            int rank = Integer.parseInt(row.get(0).toString());
            String name = row.get(1).toString();
            int rating = Integer.parseInt(row.get(2).toString());
            String playerId = row.get(3).toString();
            ladderRanking.setRank(rank);
            ladderRanking.setName(name);
            ladderRanking.setRating(rating);
            ladderRanking.setPlayerId(playerId);
        }

    }

}
