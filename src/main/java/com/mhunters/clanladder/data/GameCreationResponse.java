package com.mhunters.clanladder.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GameCreationResponse {
    private String termsOfUse;
    @JsonProperty("gameID")
    private int gameId;

}
