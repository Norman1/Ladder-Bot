package com.mhunters.clanladder.data;

import lombok.Data;

@Data
public class GameHistory {
    private int gameId;
    private String creationDate;
    private String p1Token;
    private String p2Token;
    private String state;
    private String p1State;
    private String p2State;
}
