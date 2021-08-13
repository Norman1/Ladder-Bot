package com.mhunters.clanladder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GoogleSheetSynchronizationTest {

    @Autowired
    private GoogleSheetSynchronization googleSheetSynchronization;

    @Test
    void synchronizeFileSystemDataTest() {
        googleSheetSynchronization.synchronizeFileSystemData();
    }

    @Test
    void synchronizeGoogleSheet() {
        googleSheetSynchronization.synchronizeGoogleSheet();
    }

}
