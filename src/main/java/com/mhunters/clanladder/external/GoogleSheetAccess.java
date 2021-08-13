package com.mhunters.clanladder.external;

import com.mhunters.clanladder.data.googlesheet.GoogleSheetRanking;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetSignup;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * This class encapsulates the Google sheet access.
 */
@Service
public class GoogleSheetAccess {
    private static final String BASE_PATH = "https://sheet.best/api/sheets/a38c8090-ed6d-4bef-bbdf-79f9622803af/tabs/";
    private static final String TEMPLATES_PATH = BASE_PATH + "Templates";
    private static final String SIGNUP_PATH = BASE_PATH + "Signup";
    private static final String RANKINGS_DELETION_PATH = BASE_PATH + "Rankings/Rank/*";
    private static final String RANKINGS_POST_PATH = BASE_PATH + "Rankings";

    public List<GoogleSheetTemplate> readTemplates() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<GoogleSheetTemplate>> response =
                restTemplate.exchange(TEMPLATES_PATH,
                        HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                        });

        return response.getBody();
    }


    public List<GoogleSheetSignup> readSignups() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<GoogleSheetSignup>> response =
                restTemplate.exchange(SIGNUP_PATH,
                        HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                        });
        return response.getBody();
    }

    public void deleteRankings() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(RANKINGS_DELETION_PATH);
    }

    public void insertRankings(List<GoogleSheetRanking> googleSheetRankings) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForLocation(RANKINGS_POST_PATH, googleSheetRankings);
    }


}
