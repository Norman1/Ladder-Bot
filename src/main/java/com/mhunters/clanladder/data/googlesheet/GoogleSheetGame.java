package com.mhunters.clanladder.data.googlesheet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleSheetGame {

    @JsonProperty("Report Date")
    private String reportDate;

    @JsonProperty("Player 1")
    private String player1Name;

    @JsonProperty("Player 2")
    private String player2Name;

    @JsonProperty("Result")
    private String result;

    @JsonProperty("Link")
    private String link;
}
