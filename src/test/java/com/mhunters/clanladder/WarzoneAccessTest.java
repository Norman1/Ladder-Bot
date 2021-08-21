package com.mhunters.clanladder;

import com.mhunters.clanladder.data.warzone.*;
import com.mhunters.clanladder.external.WarzoneAccess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WarzoneAccessTest {

    @Autowired
    private WarzoneAccess warzoneAccess;

    @Value("${hostEmail}")
    private String hostEmail;
    @Value("${hostApiToken}")
    private String hostApiToken;

    @Test
    void readGameTest() {
        GameQueryResponse response = warzoneAccess.readGame(27851331, hostEmail, hostApiToken);
        System.out.println(response);
    }

    @Test
    void readNonExistingGameTest() {
        GameQueryResponse response = warzoneAccess.readGame(278513310, hostEmail, hostApiToken);
        System.out.println(response);
    }


    @Test
    void createGameTest() {
        GameCreationRequest gcr = new GameCreationRequest();
        gcr.setHostEmail(hostEmail);
        gcr.setHostApiToken(hostApiToken);
        gcr.setTemplateId(1390042);
        gcr.setGameName("Temp test game");
        gcr.setPersonalMessage("-");
        GameCreationRequest.Player p1 = new GameCreationRequest.Player("5917719105", "None");
        GameCreationRequest.Player p2 = new GameCreationRequest.Player("2323867790", "None");
        GameCreationResponse gameCreationResponse = warzoneAccess.createGame(gcr);
        System.out.println(gameCreationResponse);

    }

    @Test
    void deleteGameTest() {
        GameDeletionRequest gdr = new GameDeletionRequest(hostEmail, hostApiToken, 27992579);
        DeleteGameResponse response = warzoneAccess.deleteGame(gdr);
        System.out.println(response);
    }


}
