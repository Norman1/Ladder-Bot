package com.mhunters.clanladder.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String name;
    private String inviteToken;
    private int maxGames;
    private int elo;
    private int currentGameCount;
}
