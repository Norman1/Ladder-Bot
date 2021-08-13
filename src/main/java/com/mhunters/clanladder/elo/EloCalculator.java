package com.mhunters.clanladder.elo;

import org.springframework.stereotype.Service;

@Service
public class EloCalculator {

    private static final int K_FACTOR = 100;


    public int calculateNewEloRating(double ownRating, double opponentRating, boolean isVictory) {

        double winProbability = getWinProbability(opponentRating, ownRating);
        int newEloRating;

        if (isVictory) {
            newEloRating = (int) Math.ceil(ownRating + K_FACTOR * (1 - winProbability));
        } else {
            newEloRating = (int) Math.floor(ownRating + K_FACTOR * (0 - winProbability));
        }
        return Math.round(newEloRating);
    }

    private double getWinProbability(double ownRating, double opponentRating) {
        return 1.0 * 1.0 / (1 + 1.0 * (double) (Math.pow(10, 1.0 *
                (ownRating - opponentRating) / 400)));
    }

}
