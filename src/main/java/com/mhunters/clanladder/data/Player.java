package com.mhunters.clanladder.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String name;

    private String inviteToken;
    private int maxGames;
    private int elo;
    private int currentGameCount;
    // true if the player had assigned games but failed to join any of them
    private boolean isAllGamesDenier;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }

    public int getMaxGames() {
        return maxGames;
    }

    public void setMaxGames(int maxGames) {
        this.maxGames = maxGames;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public int getCurrentGameCount() {
        return currentGameCount;
    }

    public void setCurrentGameCount(int currentGameCount) {
        this.currentGameCount = currentGameCount;
    }

    public boolean isAllGamesDenier() {
        return isAllGamesDenier;
    }

    public void setAllGamesDenier(boolean allGamesDenier) {
        isAllGamesDenier = allGamesDenier;
    }


}
