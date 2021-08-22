package com.mhunters.clanladder.external;

import com.mhunters.clanladder.data.GameHistory;
import com.mhunters.clanladder.data.Player;
import com.mhunters.clanladder.data.Template;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class FileSystemAccessTest {

    @Autowired
    private FileSystemAccess fileSystemAccess;

    @Test
    void loadPlayersTest() {
        List<Player> players = fileSystemAccess.loadPlayers();
        Assertions.assertTrue(players.size() > 0);
    }

    @Test
    void loadTemplatesTest() {
        List<Template> templates = fileSystemAccess.loadTemplates();
        Assertions.assertTrue(templates.size() > 0);
    }

    @Test
    void loadGamesTest() {
        List<GameHistory> games = fileSystemAccess.loadGames();
        Assertions.assertTrue(games.size() > 0);
    }

    @Test
    void replaceTemplatesTest() {
        List<Template> templates = fileSystemAccess.loadTemplates();
        fileSystemAccess.replaceTemplates(templates);
        List<Template> templates2 = fileSystemAccess.loadTemplates();
        Assertions.assertTrue(templates2.size() > 0);
    }

    @Test
    void replaceGamesTest() {
        List<GameHistory> games = fileSystemAccess.loadGames();
        fileSystemAccess.replaceGames(games);
        List<GameHistory> games2 = fileSystemAccess.loadGames();
        Assertions.assertTrue(games2.size() > 0);
    }

    @Test
    void addHistoricGamesTest() {
        List<GameHistory> games = fileSystemAccess.loadGames();
        games = games.subList(0,2);
        fileSystemAccess.addHistoricGames(games);
        fileSystemAccess.addHistoricGames(games);
        fileSystemAccess.addHistoricGames(games);
    }


}
