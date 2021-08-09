package com.mhunters.clanladder.external;

import com.mhunters.clanladder.data.GameCreationRequest;
import com.mhunters.clanladder.data.GameCreationResponse;
import com.mhunters.clanladder.data.GameQueryResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * This class encapsulates the WarZone access.
 */
@Service
public class WarzoneAccess {

    private static final String CREATE_GAME_ENDPOINT = "https://www.warzone.com/API/CreateGame";

    public GameCreationResponse createGame(GameCreationRequest gameCreationRequest) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<GameCreationResponse> response = restTemplate.postForEntity(CREATE_GAME_ENDPOINT, gameCreationRequest, GameCreationResponse.class);
        return response.getBody();
    }

    public GameQueryResponse readGame(int gameId, String email, String apiToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("Email", email);
        map.add("APIToken", apiToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        String url = "https://www.warzone.com/API/GameFeed?GameID=" + gameId;

        ResponseEntity<GameQueryResponse> response = restTemplate.exchange(url,
                HttpMethod.POST,
                entity,
                GameQueryResponse.class);
        return response.getBody();
    }


}
