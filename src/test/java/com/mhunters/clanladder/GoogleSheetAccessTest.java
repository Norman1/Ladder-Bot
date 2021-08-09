package com.mhunters.clanladder;

import com.mhunters.clanladder.external.GoogleSheetAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GoogleSheetAccessTest {

    @Autowired
    private GoogleSheetAccess googleSheetAccess;

//    @Test
//    void loadGamesTest() {
//        List<GameHistory> gameHistories = googleSheetAccess.loadGames();
//        Assertions.assertTrue(gameHistories.size() > 0);
//    }
//
//    @Test
//    void saveGamesTest() {
//        List<GameHistory> gameHistories = googleSheetAccess.loadGames();
//        googleSheetAccess.saveGames(gameHistories, false);
//        List<GameHistory> gameHistories2 = googleSheetAccess.loadGames();
//        Assertions.assertTrue(gameHistories2.size() > 0);
//    }
//
//    @Test
//    void loadPlayersTest() {
//        List<Player> players = googleSheetAccess.loadPlayers();
//        System.out.println(players);
//    }
}
