package com.mhunters.clanladder.data.warzone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * The Warzone response from a game query. In the error case, the result is also 200 and the error property is set but
 * not other properties. If there is no error, the error property is not set.
 */
@Data
public class GameQueryResponse {
    private int id;
    private String state;
    private String created;
    private String lastTurnTime;
    @JsonProperty("templateID")
    private int templateId;
    private List<GamePlayerQueryResponse> players;
    private String error;

    @Data
    public static class GamePlayerQueryResponse {
        private String id;
        private String name;
        private String state;
    }

}
