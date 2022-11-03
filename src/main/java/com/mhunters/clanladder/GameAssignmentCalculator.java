package com.mhunters.clanladder;

import com.mhunters.clanladder.data.GameAssignment;
import com.mhunters.clanladder.data.GameHistory;
import com.mhunters.clanladder.data.Player;
import com.mhunters.clanladder.data.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
// TODO weiter

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

            // TODO weiter
            Player p2 = chooseRandomPlayer(eligiblePlayers);
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
        // We prefer to assign players with the most non assigned games first
        eligiblePlayers.sort((p1, p2) -> getUnassignedGames(p2) - getUnassignedGames(p1));
        int bestPlayerMissingArmies = getUnassignedGames(eligiblePlayers.get(0));
        List<Player> bestPlayers = eligiblePlayers.stream().filter(p -> getUnassignedGames(p) == bestPlayerMissingArmies).collect(Collectors.toList());
        Player randomBestPlayer = chooseRandomPlayer(bestPlayers);
        return randomBestPlayer;
    }

    // Step 2
    private Player chooseBestSecondPlayer(List<Player> eligiblePlayers, Player firstPlayer) {
        List<GameHistory> gamesToConsider = new ArrayList<>();
        gamesToConsider.addAll(dataSynchronizer.getHistoricGames());
        gamesToConsider.addAll(dataSynchronizer.getNewlyFinishedGames());
        String token = firstPlayer.getInviteToken();
        gamesToConsider = gamesToConsider.stream().filter(
                game -> game.getP1Token().equals(firstPlayer.getInviteToken())
                        || game.getP2Token().equals(firstPlayer.getInviteToken())).collect(Collectors.toList());
        Collections.shuffle(eligiblePlayers);

// TODO weiter
        return null;
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
