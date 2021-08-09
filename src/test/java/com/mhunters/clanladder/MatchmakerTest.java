package com.mhunters.clanladder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MatchmakerTest {

    @Autowired
    private Matchmaker matchmaker;

    @Test
    @DisplayName("Debug test to execute the matchmaking from here.")
    void executeMatchmakingTest() {
        matchmaker.executeMatchmaking();
    }

    @Test
    void updateGameStatesTest() {
        matchmaker.updateGameStates();
    }

}
