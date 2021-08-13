package com.mhunters.clanladder.data.googlesheet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleSheetSignup {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Token")
    private String token;
    @JsonProperty("Max Games")
    private int maxGames;
}
