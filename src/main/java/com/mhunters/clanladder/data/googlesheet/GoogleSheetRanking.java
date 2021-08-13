package com.mhunters.clanladder.data.googlesheet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleSheetRanking {
    @JsonProperty("Rank")
    private int rank;

    @JsonProperty("Player Name")
    private String name;

    @JsonProperty("Rating")
    private int rating;
}
