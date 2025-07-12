package com.mhunters.clanladder.data;

import com.mhunters.clanladder.data.warzone.Game;

import java.util.List;

/**
 * This class contains all data during an application run.
 */
public class DataWrapper {
    private List<Player> players;
    private List<Template> templates;
    private List<LadderRanking> ladderRankings;

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    private List<Game> games;


    public List<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<LadderRanking> getLadderRankings() {
        return ladderRankings;
    }

    public void setLadderRankings(List<LadderRanking> ladderRankings) {
        this.ladderRankings = ladderRankings;
    }
}
