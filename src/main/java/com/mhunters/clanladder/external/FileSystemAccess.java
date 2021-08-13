package com.mhunters.clanladder.external;

import com.mhunters.clanladder.DateUtils;
import com.mhunters.clanladder.data.GameHistory;
import com.mhunters.clanladder.data.Player;
import com.mhunters.clanladder.data.Template;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the file system access.
 */
@Service
public class FileSystemAccess {

    private static final String BASE_PATH = "D:/Files/nghood/Ladder-Bot/FileSystemData/";
    private static final String TEMPLATE_FILENAME = "Templates.csv";
    private static final String GAMES_FILENAME = "Games.csv";
    private static final String PLAYERBASE_FILENAME = "Playerbase.csv";

    public List<Player> loadPlayers() {
        List<List<String>> playersCsvData = loadCsv(PLAYERBASE_FILENAME);
        List<Player> players = new ArrayList<>();
        for (List<String> playerCsvData : playersCsvData) {
            Player player = new Player();
            player.setName(playerCsvData.get(0));
            player.setInviteToken(playerCsvData.get(1));
            player.setMaxGames(Integer.parseInt(playerCsvData.get(2)));
            player.setElo(Integer.parseInt(playerCsvData.get(3)));
            players.add(player);
        }
        return players;
    }

    public List<GameHistory> loadGames() {
        List<List<String>> gamesCsvData = loadCsv(GAMES_FILENAME);
        List<GameHistory> gameHistories = new ArrayList<>();
        for (List<String> gameCsvData : gamesCsvData) {
            GameHistory gameHistory = new GameHistory();
            gameHistory.setGameId(Integer.parseInt(gameCsvData.get(0)));
            gameHistory.setCreationDate(DateUtils.parseDate(gameCsvData.get(1)));
            gameHistory.setP1Token(gameCsvData.get(2));
            gameHistory.setP2Token(gameCsvData.get(3));
            gameHistory.setState(gameCsvData.get(4));
            gameHistory.setP1State(gameCsvData.get(5));
            gameHistory.setP2State(gameCsvData.get(6));
            gameHistories.add(gameHistory);
        }
        return gameHistories;
    }

    public List<Template> loadTemplates() {
        List<List<String>> templatesCsvData = loadCsv(TEMPLATE_FILENAME);
        List<Template> templates = new ArrayList<>();
        for (List<String> templateCsvData : templatesCsvData) {
            Template template = new Template();
            template.setName(templateCsvData.get(0));
            template.setLink(templateCsvData.get(1));
            int id = Integer.parseInt(template.getLink().split("=")[1]);
            template.setId(id);
            templates.add(template);
        }
        return templates;
    }


    public void replaceGames(List<GameHistory> games) {
        List<String> headers = List.of("gameId", "creationDate", "p1Token", "p2Token", "state", "p1State", "p2state");
        List<List<String>> data = new ArrayList<>();
        for (GameHistory game : games) {
            data.add(List.of(Integer.toString(game.getGameId()),
                    DateUtils.format(game.getCreationDate()),
                    game.getP1Token(),
                    game.getP2Token(),
                    game.getState(),
                    game.getP1State(),
                    game.getP2State()));
        }
        replaceCsv(GAMES_FILENAME, headers, data);
    }

    public void replacePlayers(List<Player> players) {
        List<String> headers = List.of("Name", "InviteToken", "MaxGames", "Elo");
        List<List<String>> data = new ArrayList<>();
        for (Player player : players) {
            data.add(List.of(player.getName(),
                    player.getInviteToken(),
                    Integer.toString(player.getMaxGames()),
                    Integer.toString(player.getElo())));
        }
        replaceCsv(PLAYERBASE_FILENAME, headers, data);
    }


    public void replaceTemplates(List<Template> templates) {
        List<String> headers = List.of("Name", "Link");
        List<List<String>> data = new ArrayList<>();
        for (Template template : templates) {
            data.add(List.of(template.getName(), template.getLink()));
        }
        replaceCsv(TEMPLATE_FILENAME, headers, data);
    }

    @SneakyThrows(value = IOException.class)
    private void replaceCsv(String fileName, List<String> headers, List<List<String>> data) {
        File file = new File(BASE_PATH + fileName);
        FileWriter fw = new FileWriter(file, false);
        String[] insertHeaders = headers.toArray(String[]::new);
        CSVPrinter csvPrinter = new CSVPrinter(fw, CSVFormat.DEFAULT.withHeader(insertHeaders));
        try {
            for (List<String> row : data) {
                String[] insertRow = row.toArray(String[]::new);
                csvPrinter.printRecord(insertRow);
            }
        } finally {
            fw.flush();
            csvPrinter.flush();
        }
    }

    @SneakyThrows(value = IOException.class)
    private List<List<String>> loadCsv(String fileName) {
        File file = new File(BASE_PATH + fileName);
        Reader reader = Files.newBufferedReader(file.toPath());
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
        List<List<String>> out = new ArrayList<>();
        try {
            List<CSVRecord> records = csvParser.getRecords();
            for (CSVRecord record : records) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < record.size(); i++) {
                    row.add(record.get(i));
                }
                out.add(row);
            }
            return out;
        } finally {
            csvParser.close();
        }
    }
}
