package com.mhunters.clanladder.elo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EloCalculatorTest {

    @Autowired
    private EloCalculator eloCalculator;

    @Test
    void calculateNewEloRatingTest() {
        int ratingVictory = eloCalculator.calculateNewEloRating(1000, 1000, true);
        int ratingDefeat = eloCalculator.calculateNewEloRating(1000, 1000, false);
        Assertions.assertEquals(1000 + 1000, ratingVictory + ratingDefeat);

        int ratingBigVictory = eloCalculator.calculateNewEloRating(1000, 1500, true);
        int ratingBigDefeat = eloCalculator.calculateNewEloRating(1500, 1000, false);
        Assertions.assertEquals(1000 + 1500, ratingBigVictory + ratingBigDefeat);

        System.out.println(ratingVictory);
        System.out.println(ratingDefeat);
        System.out.println(ratingBigVictory);
        System.out.println(ratingBigDefeat);
    }
}
