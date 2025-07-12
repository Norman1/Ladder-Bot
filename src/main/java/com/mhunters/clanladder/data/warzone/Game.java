package com.mhunters.clanladder.data.warzone;

public class Game {

    private String link;
    private String player1Token;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPlayer1Token() {
        return player1Token;
    }

    public void setPlayer1Token(String player1Token) {
        this.player1Token = player1Token;
    }

    public String getPlayer2Token() {
        return player2Token;
    }

    public void setPlayer2Token(String player2Token) {
        this.player2Token = player2Token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String player2Token;
    private String status;


}
