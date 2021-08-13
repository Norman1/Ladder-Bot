package com.mhunters.clanladder.elo;

import com.mhunters.clanladder.DataSynchronizer;
import com.mhunters.clanladder.data.GameHistory;
import com.mhunters.clanladder.data.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EloUpdater {

    @Autowired
    private DataSynchronizer dataSynchronizer;
    @Autowired
    private EloCalculator eloCalculator;

    private enum WinState {
        DRAW, VICTORY, DEFEAT
    }

    public void updateEloRankings() {
        List<Player> players = dataSynchronizer.getAllPlayers();
        List<GameHistory> deadOrOutdatedGames = dataSynchronizer.getDeadOrOutdatedGames();
        List<GameHistory> newlyFinishedGames = dataSynchronizer.getNewlyFinishedGames();
        List<GameHistory> allConsideredGames = new ArrayList<>();
        allConsideredGames.addAll(newlyFinishedGames);
        allConsideredGames.addAll(deadOrOutdatedGames);
        for (GameHistory game : allConsideredGames) {
            Player p1 = players.stream().filter(p -> p.getInviteToken().equals(game.getP1Token())).findAny().get();
            Player p2 = players.stream().filter(p -> p.getInviteToken().equals(game.getP2Token())).findAny().get();

            WinState p1WinState = calculateWinState(game);
            int p1Rating = p1.getElo();
            int p2Rating = p2.getElo();
            int updatedP1Rating;
            int updatedP2Rating;
            if (p1WinState.equals(WinState.DRAW)) {
                // a draw is Elo neutral
                updatedP1Rating = p1Rating;
                updatedP2Rating = p2Rating;
            } else if (p1WinState.equals(WinState.VICTORY)) {
                updatedP1Rating = eloCalculator.calculateNewEloRating(p1Rating, p2Rating, true);
                updatedP2Rating = eloCalculator.calculateNewEloRating(p2Rating, p1Rating, false);
            } else {
                updatedP1Rating = eloCalculator.calculateNewEloRating(p1Rating, p2Rating, false);
                updatedP2Rating = eloCalculator.calculateNewEloRating(p2Rating, p1Rating, true);
            }
            p1.setElo(updatedP1Rating);
            p2.setElo(updatedP2Rating);
        }
        Collections.sort(players, (p1, p2) -> p2.getElo() - p1.getElo());
    }


    // calculates the win state from player 1s perspective
    private WinState calculateWinState(GameHistory gameHistory) {
        String p1State = gameHistory.getP1State();
        String p2State = gameHistory.getP2State();
        boolean p1FailedToJoin = p1State.equals("Invited") || p1State.equals("Declined");
        boolean p2FailedToJoin = p2State.equals("Invited") || p2State.equals("Declined");

        if (p1State.equals("EndedByVote") || (p1FailedToJoin && p2FailedToJoin)) {
            return WinState.DRAW;
        }
        if (p1State.equals("Won") || p2FailedToJoin) {
            return WinState.VICTORY;
        }
        if (p2State.equals("Won") || p1FailedToJoin) {
            return WinState.DEFEAT;
        }
        // not supposed to happen
        return null;
    }


}

