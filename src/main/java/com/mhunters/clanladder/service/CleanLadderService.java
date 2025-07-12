package com.mhunters.clanladder.service;

import com.mhunters.clanladder.data.DataWrapper;
import com.mhunters.clanladder.data.LadderRanking;
import com.mhunters.clanladder.data.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Cleans the ladder if the signups do not match the ladder
 */
@Service
public class CleanLadderService {

    private final Logger logger = Logger.getLogger("CleanLadderService");

    private static final int DEFAULT_START_ELO = 1000;


    public void cleanLadder(DataWrapper dataWrapper) {
        logger.info("Called CleanLadderService.cleanLadder");
        removePlayersMissingInSignup(dataWrapper);
        addPlayersNewInSignup(dataWrapper);
        orderLadderRankings(dataWrapper);

    }


    private void orderLadderRankings(DataWrapper dataWrapper) {
        List<LadderRanking> ladderRankings = dataWrapper.getLadderRankings();
        ladderRankings.sort(Comparator.comparingInt(LadderRanking::getRating));
        for (int i = 0; i < ladderRankings.size(); i++) {
            LadderRanking ladderRanking = ladderRankings.get(i);
            ladderRanking.setRank(i + 1);
        }

    }

    private void removePlayersMissingInSignup(DataWrapper dataWrapper) {
        List<Player> players = dataWrapper.getPlayers();
        List<LadderRanking> ladderRankings = dataWrapper.getLadderRankings();
        List<LadderRanking> rankingsToRemove = new ArrayList<>();
        for (LadderRanking ladderRanking : ladderRankings) {
            boolean isPlayerInSignup = false;
            for (Player player : players) {
                if (player.getInviteToken().equals(ladderRanking.getPlayerId())) {
                    isPlayerInSignup = true;
                    break;
                }
            }
            if (!isPlayerInSignup) {
                rankingsToRemove.add(ladderRanking);
            }
        }
        ladderRankings.removeAll(rankingsToRemove);
    }

    private void addPlayersNewInSignup(DataWrapper dataWrapper) {
        List<Player> players = dataWrapper.getPlayers();
        List<LadderRanking> ladderRankings = dataWrapper.getLadderRankings();
        for (Player player : players) {
            boolean isPlayerInLadder = isPlayerExistInLadder(player, ladderRankings);
            if (!isPlayerInLadder) {
                LadderRanking ladderRanking = new LadderRanking();
                ladderRanking.setPlayerId(player.getInviteToken());
                ladderRanking.setName(player.getName());
                ladderRanking.setRank(-1);
                ladderRanking.setRating(DEFAULT_START_ELO);
                ladderRankings.add(ladderRanking);
            }
        }
    }

    private boolean isPlayerExistInLadder(Player player, List<LadderRanking> ladderRankings) {
        for (LadderRanking ranking : ladderRankings) {
            if (ranking.getPlayerId().equals(player.getInviteToken())) {
                return true;
            }
        }
        return false;
    }

//    private boolean isPlayerExistInSignup(Player player, List<Player> players) {
//        for (Player p : players) {
//            if (p.getInviteToken().equals(player.getId())) {
//                return true;
//            }
//        }
//        return false;
//    }

}
