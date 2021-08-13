package com.mhunters.clanladder;

import com.mhunters.clanladder.data.*;
import com.mhunters.clanladder.external.WarzoneAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Matchmaker {

    private static final String GAME_NAME = "M'Hunters Ladder Game";
    private static final String TEAM = "None";


    @Autowired
    private WarzoneAccess warzoneAccess;

    @Autowired
    private DataSynchronizer dataSynchronizer;

    @Value("${hostEmail}")
    private String hostEmail;
    @Value("${hostApiToken}")
    private String hostApiToken;

    /**
     * This is the main method to trigger the whole matchmaking process. It automatically triggers once per day
     * at 10pm.
     */
    @Scheduled(cron = "0 0 22 * * *")
    public void executeProcess() {
        log.info("Called executeProcess");
        /*
         * Step 1: Load all data
         * Step 2: Create new matches
         * Step 3: update all data at end
         */
        dataSynchronizer.loadStartState();
        executeMatchMaking();
        dataSynchronizer.updateAtEnd();
    }

    private void executeMatchMaking() {
        List<Player> players = dataSynchronizer.getAllPlayers();
        List<Template> templates = dataSynchronizer.getAllTemplates();
        List<GameAssignment> gameAssignments = assignGames(players, templates);
        List<GameCreationRequest> gameCreationRequests = createGameCreationRequests(gameAssignments);
        List<GameHistory> createdGames = new ArrayList<>();
        for (GameCreationRequest gameCreationRequest : gameCreationRequests) {
            GameCreationResponse gameCreationResponse = warzoneAccess.createGame(gameCreationRequest);
            // The game could not get created
            if (gameCreationResponse.getGameId() == 0) {
                log.warn("Failed game creation: " + gameCreationRequest);
                continue;
            }
            GameHistory gameHistory = new GameHistory();
            gameHistory.setGameId(gameCreationResponse.getGameId());
            gameHistory.setCreationDate(LocalDateTime.now());
            gameHistory.setP1Token(gameCreationRequest.getPlayers().get(0).getToken());
            gameHistory.setP2Token(gameCreationRequest.getPlayers().get(1).getToken());
            gameHistory.setState("WaitingForPlayers");
            gameHistory.setP1State("Invited");
            gameHistory.setP2State("Invited");
            createdGames.add(gameHistory);
        }
        dataSynchronizer.setNewlyCreatedGames(createdGames);
    }

    private List<GameCreationRequest> createGameCreationRequests(List<GameAssignment> gameAssignments) {
        List<GameCreationRequest> gameCreationRequests = new ArrayList<>();
        for (GameAssignment gameAssignment : gameAssignments) {
            GameCreationRequest gameCreationRequest = new GameCreationRequest();
            gameCreationRequest.setHostEmail(hostEmail);
            gameCreationRequest.setHostApiToken(hostApiToken);
            gameCreationRequest.setTemplateId(gameAssignment.getTemplateId());
            gameCreationRequest.setGameName(GAME_NAME);
            gameCreationRequest.setPersonalMessage(generatePersonalMessage(gameAssignment));
            GameCreationRequest.Player p1 = new GameCreationRequest.Player(gameAssignment.getPlayer1Token(), TEAM);
            GameCreationRequest.Player p2 = new GameCreationRequest.Player(gameAssignment.getPlayer2Token(), TEAM);
            gameCreationRequest.setPlayers(List.of(p1, p2));
            gameCreationRequests.add(gameCreationRequest);
        }
        return gameCreationRequests;
    }

    private String generatePersonalMessage(GameAssignment gameAssignment) {
        List<Player> players = dataSynchronizer.getAllPlayers().stream().
                filter(p -> gameAssignment.getPlayer1Token().equals(p.getInviteToken()) ||
                        gameAssignment.getPlayer2Token().equals(p.getInviteToken())).collect(Collectors.toList());

        List<Player> allPlayingPlayers = dataSynchronizer.getAllPlayers().stream().filter(p -> p.getMaxGames() > 0).collect(Collectors.toList());
        Collections.sort(allPlayingPlayers, (p1, p2) -> p2.getElo() - p1.getElo());

        Player contender1 = players.get(0);
        Player contender2 = players.get(1);
        String out = "This game is part of the M'Hunters internal ladder.\n";
        int rankP1 = allPlayingPlayers.indexOf(contender1) + 1;
        int rankP2 = allPlayingPlayers.indexOf(contender2) + 1;
        out += "Contender 1: " + contender1.getName() + " (Rank " + rankP1 + " with a rating of " + contender1.getElo() + ")\n";
        out += "Contender 2: " + contender2.getName() + " (Rank " + rankP2 + " with a rating of " + contender2.getElo() + ")";
        return out;
    }


    private List<GameAssignment> assignGames(List<Player> players, List<Template> templates) {
        List<GameAssignment> gameAssignments = new ArrayList<>();
        List<Player> eligiblePlayers = getEligiblePlayers(players);
        while (eligiblePlayers.size() >= 2) {
            Player p1 = chooseRandomPlayer(eligiblePlayers);
            p1.setCurrentGameCount(p1.getCurrentGameCount() + 1);
            eligiblePlayers.remove(p1);
            Player p2 = chooseRandomPlayer(eligiblePlayers);
            p2.setCurrentGameCount(p2.getCurrentGameCount() + 1);
            Template template = chooseRandomTemplate(templates);
            GameAssignment gameAssignment = new GameAssignment(template.getId(), p1.getInviteToken(), p2.getInviteToken());
            gameAssignments.add(gameAssignment);
            eligiblePlayers = getEligiblePlayers(players);
        }
        return gameAssignments;
    }

    private List<Player> getEligiblePlayers(List<Player> players) {
        return players.stream().filter(p -> p.getMaxGames() > p.getCurrentGameCount() + p.getNewlyAssignedGames()).collect(Collectors.toList());
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
