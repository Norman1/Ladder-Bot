package com.mhunters.clanladder.data.warzone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * The Warzone response for a game creation request.
 */
@Data
public class GameCreationResponse {
    private String termsOfUse;
    @JsonProperty("gameID")
    private int gameId;
    private String error;

}
