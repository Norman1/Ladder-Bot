package com.mhunters.clanladder.data.warzone;

import lombok.Data;

/**
 * The Warzone response for a game deletion request.
 */
@Data
public class DeleteGameResponse {
    private String success;
    private String error;
}
