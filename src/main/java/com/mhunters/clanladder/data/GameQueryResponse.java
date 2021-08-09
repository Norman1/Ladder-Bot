package com.mhunters.clanladder.data;

import lombok.Data;

import java.util.List;

@Data
public class GameQueryResponse {
    private String state;
    private String created;
    private List<GamePlayerQueryResponse> players;

    @Data
    public static class GamePlayerQueryResponse {
        private String id;
        private String state;
    }

}
