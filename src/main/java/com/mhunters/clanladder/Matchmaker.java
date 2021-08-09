package com.mhunters.clanladder;

import com.mhunters.clanladder.data.*;
import com.mhunters.clanladder.external.FileSystemAccess;
import com.mhunters.clanladder.external.WarzoneAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class Matchmaker {

    private static final String GAME_NAME = "M'Hunters Auto Game";
    private static final String PERSONAL_MESSAGE = "This game was automatically created. Please visit our Discord to see more. Please do not boot your opponent. It has practice settings and autoboot is turned off. Have fun and discuss strategy :)";
    private static final String TEAM = "None";

    @Autowired
    private WarzoneAccess warzoneAccess;

    @Autowired
    private FileSystemAccess fileSystemAccess;

    @Value("${hostEmail}")
    private String hostEmail;
    @Value("${hostApiToken}")
    private String hostApiToken;

    /**
     * This is the main method to trigger the whole matchmaking process.
     */
    public void executeMatchmaking() {
        updateGameStates();
        List<Player> players = fileSystemAccess.loadPlayers();
        List<Template> templates = fileSystemAccess.loadTemplates();
        List<GameAssignment> gameAssignments = assignGames(players, templates);
        List<GameCreationRequest> gameCreationRequests = createGameCreationRequests(gameAssignments);
        List<GameHistory> createdGames = new ArrayList<>();
        for (GameCreationRequest gameCreationRequest : gameCreationRequests) {
            GameCreationResponse gameCreationResponse = warzoneAccess.createGame(gameCreationRequest);
            GameHistory gameHistory = new GameHistory();
            gameHistory.setGameId(gameCreationResponse.getGameId());
            gameHistory.setP1Token(gameCreationRequest.getPlayers().get(0).getToken());
            gameHistory.setP2Token(gameCreationRequest.getPlayers().get(1).getToken());
            gameHistory.setState("WaitingForPlayers");
            createdGames.add(gameHistory);
        }
        List<GameHistory> allGames = fileSystemAccess.loadGames();
        allGames.addAll(createdGames);
        fileSystemAccess.replaceGames(allGames);
    }

    public void updateGameStates() {
        List<GameHistory> allGames = fileSystemAccess.loadGames();
        for (GameHistory gameHistory : allGames) {
            // TODO erroneous condition
//            boolean isGameOngoing = gameHistory.getState().equals("WaitingForPlayers")
//                    || gameHistory.getState().equals("DistributingTerritories")
//                    || gameHistory.getState().equals("Playing");
//            if (!isGameOngoing) {
//                continue;
//            }
            GameQueryResponse gameQueryResponse = warzoneAccess.readGame(gameHistory.getGameId(), hostEmail, hostApiToken);
            gameHistory.setState(gameQueryResponse.getState());
            gameHistory.setCreationDate(gameQueryResponse.getCreated());
            List<GameQueryResponse.GamePlayerQueryResponse> players = gameQueryResponse.getPlayers();
            gameHistory.setP1State(players.stream().filter(p -> p.getId().equals(gameHistory.getP1Token())).findAny().get().getState());
            gameHistory.setP2State(players.stream().filter(p -> p.getId().equals(gameHistory.getP2Token())).findAny().get().getState());
        }
        fileSystemAccess.replaceGames(allGames);
    }


    private List<GameCreationRequest> createGameCreationRequests(List<GameAssignment> gameAssignments) {
        List<GameCreationRequest> gameCreationRequests = new ArrayList<>();
        for (GameAssignment gameAssignment : gameAssignments) {
            GameCreationRequest gameCreationRequest = new GameCreationRequest();
            gameCreationRequest.setHostEmail(hostEmail);
            gameCreationRequest.setHostApiToken(hostApiToken);
            gameCreationRequest.setTemplateId(gameAssignment.getTemplateId());
            gameCreationRequest.setGameName(GAME_NAME);
            gameCreationRequest.setPersonalMessage(PERSONAL_MESSAGE);
            GameCreationRequest.Player p1 = new GameCreationRequest.Player(gameAssignment.getPlayer1Token(), TEAM);
            GameCreationRequest.Player p2 = new GameCreationRequest.Player(gameAssignment.getPlayer2Token(), TEAM);
            gameCreationRequest.setPlayers(List.of(p1, p2));
            gameCreationRequests.add(gameCreationRequest);
        }
        return gameCreationRequests;
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
