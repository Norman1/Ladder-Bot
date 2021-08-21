package com.mhunters.clanladder.data.warzone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class GameCreationRequest {

    private String hostEmail;
    @JsonProperty("hostAPIToken")
    private String hostApiToken;
    @JsonProperty("templateID")
    private int templateId;
    private String gameName;
    private String personalMessage;
    private List<Player> players;

    @Data
    @AllArgsConstructor
    public static class Player {
        private String token;
        private String team;
    }

}
