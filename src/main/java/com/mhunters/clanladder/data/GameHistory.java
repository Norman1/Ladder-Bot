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
    private LocalDateTime lastTurnDate;
    private int templateId;
    private String p1Token;
    private String p2Token;
    private String state;
    private String p1State;
    private String p2State;
    private SheetReportInfo sheetReportInfo = new SheetReportInfo();

    @Data
    public static class SheetReportInfo {
        private String player1Name;
        private String player2Name;
        private String result;
    }
}

