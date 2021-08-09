package com.mhunters.clanladder.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameAssignment {
    private int templateId;
    private String player1Token;
    private String player2Token;
}
