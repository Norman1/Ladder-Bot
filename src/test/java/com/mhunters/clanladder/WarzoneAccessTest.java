package com.mhunters.clanladder;

import com.mhunters.clanladder.data.GameQueryResponse;
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
}
