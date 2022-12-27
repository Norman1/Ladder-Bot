package com.mhunters.clanladder;

import com.mhunters.clanladder.data.GameAssignment;
import com.mhunters.clanladder.data.Player;
import com.mhunters.clanladder.data.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This class is responsible for finding optimal game assignments.
 */
@Service
@Slf4j
public class GameAssignmentCalculator {

    @Autowired
    private DataSynchronizer dataSynchronizer;


    public List<GameAssignment> assignGames(List<Player> players, List<Template> templates) {

        List<GameAssignment> gameAssignments = new ArrayList<>();
        List<Player> eligiblePlayers = getEligiblePlayers(players);
        while (eligiblePlayers.size() >= 2) {
            /*
             * Step 1: Choose player p1
             * Step 2: Choose the best opponent p2 for player p1
             * Step 3: Choose the best template for both p1 and p2.
             */
            Player p1 = chooseBestFirstPlayer(eligiblePlayers);
            p1.setCurrentGameCount(p1.getCurrentGameCount() + 1);
            eligiblePlayers.remove(p1);
            Player p2 = chooseBestSecondPlayer(eligiblePlayers);
            p2.setCurrentGameCount(p2.getCurrentGameCount() + 1);
            Template template = chooseRandomTemplate(templates);
            GameAssignment gameAssignment = new GameAssignment(template.getId(), p1.getInviteToken(), p2.getInviteToken());
            gameAssignments.add(gameAssignment);
            eligiblePlayers = getEligiblePlayers(players);
        }
        return gameAssignments;
    }

    // Step 1
    private Player chooseBestFirstPlayer(List<Player> eligiblePlayers) {
        // If there are players who decline their games, we assign them first.
        List<Player> decliningPlayers = eligiblePlayers.stream().filter(p -> p.isAllGamesDenier()).collect(Collectors.toList());
        if (decliningPlayers.size() > 0) {
            return chooseRandomPlayer(decliningPlayers);
        }

        // We prefer to assign players with the most non assigned games first
        eligiblePlayers.sort((p1, p2) -> getUnassignedGames(p2) - getUnassignedGames(p1));
        int bestPlayerMissingGames = getUnassignedGames(eligiblePlayers.get(0));
        List<Player> bestPlayers = eligiblePlayers.stream().filter(p -> getUnassignedGames(p) == bestPlayerMissingGames).collect(Collectors.toList());
        Player randomBestPlayer = chooseRandomPlayer(bestPlayers);
        return randomBestPlayer;
    }

    private Player chooseBestSecondPlayer(List<Player> eligiblePlayers) {
        // If there are players who decline their games, we assign them first.
        List<Player> decliningPlayers = eligiblePlayers.stream().filter(p -> p.isAllGamesDenier()).collect(Collectors.toList());
        if (decliningPlayers.size() > 0) {
            return chooseRandomPlayer(decliningPlayers);
        }
        return chooseRandomPlayer(eligiblePlayers);
    }


    private int getUnassignedGames(Player player) {
        return player.getMaxGames() - player.getCurrentGameCount();
    }


    private List<Player> getEligiblePlayers(List<Player> players) {
        return players.stream().filter(p -> p.getMaxGames() > p.getCurrentGameCount()).collect(Collectors.toList());
    }

    private Player chooseRandomPlayer(List<Player> players) {
        Random random = new Random();
        return players.get(random.nextInt(players.size()));
    }

    private Template chooseRandomTemplate(List<Template> templates) {
        Random random = new Random();
        return templates.get(random.nextInt(templates.size()));
    }

}
