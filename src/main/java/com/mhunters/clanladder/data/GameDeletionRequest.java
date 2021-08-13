package com.mhunters.clanladder.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameDeletionRequest {
    @JsonProperty("Email")
    private String email;
    @JsonProperty("APIToken")
    private String apiToken;
    @JsonProperty("gameID")
    private int gameId;
}
