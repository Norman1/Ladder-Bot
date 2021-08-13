package com.mhunters.clanladder;

import com.mhunters.clanladder.data.GameAssignment;
import com.mhunters.clanladder.data.Player;
import com.mhunters.clanladder.data.Template;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MatchmakerTest {

    @Autowired
    private Matchmaker matchmaker;

    @Test
    @DisplayName("Debug test to execute the cronjob manually")
    void executeProcessTest() {
        matchmaker.executeProcess();
    }

    @Test
    void assignGamesTest() {
        Template t1 = new Template();
        Player p1 = new Player("A", "1", 2, 1000, 0);
        Player p2 = new Player("B", "1", 3, 1000, 0);
        List<Player> playerList = List.of(p1, p2);
        List<GameAssignment> gameAssignments = matchmaker.assignGames(playerList, List.of(t1));
        System.out.println(gameAssignments);
    }

}
