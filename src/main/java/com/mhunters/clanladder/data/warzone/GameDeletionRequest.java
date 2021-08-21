package com.mhunters.clanladder.data.warzone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Request object for calling the Warzone game deletion API.
 */
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
