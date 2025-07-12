package com.mhunters.clanladder.service;

import com.mhunters.clanladder.data.DataWrapper;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * The main class which orchestrates the whole process.
 */
@Service
public class Orchestrator {

    private final AccessGoogleService googleAccessService;
    private final CleanLadderService cleanLadderService;

    public Orchestrator(AccessGoogleService googleAccessService, CleanLadderService cleanLadderService) {
        this.googleAccessService = googleAccessService;
        this.cleanLadderService = cleanLadderService;
    }

    private Logger logger = Logger.getLogger("Orchestrator");

    public void runAll() {
        logger.info("Called Orchestrator.runAll");
        DataWrapper dataWrapper = new DataWrapper();
        googleAccessService.reaadAllData(dataWrapper);
        cleanLadderService.cleanLadder(dataWrapper);
        googleAccessService.writeAllData(dataWrapper);

        logger.info("Orchestrator successfully ran through");

    }

}
