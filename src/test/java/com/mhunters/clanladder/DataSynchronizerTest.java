package com.mhunters.clanladder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DataSynchronizerTest {

    @Autowired
    private DataSynchronizer dataSynchronizer;

    @Test
    void synchronizeAtStartTest() {
        dataSynchronizer.loadStartState();
        System.out.println(dataSynchronizer.getAllTemplates());
        System.out.println(dataSynchronizer.getAllPlayers());
        System.out.println(dataSynchronizer.getOngoingGames());
        System.out.println(dataSynchronizer.getDeadOrOutdatedGames());
        System.out.println(dataSynchronizer.getNewlyFinishedGames());
    }


}
