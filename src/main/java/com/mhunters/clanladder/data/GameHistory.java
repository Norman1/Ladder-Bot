package com.mhunters.clanladder.data;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * The internally used data structure for the game history.
 */
@Data
public class GameHistory {
    private int gameId;
    private LocalDateTime creationDate;
    private String p1Token;
    private String p2Token;
    private String state;
    private String p1State;
    private String p2State;
}
