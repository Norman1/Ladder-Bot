package com.mhunters.clanladder;

import com.mhunters.clanladder.service.GoogleSheetsService;
import com.mhunters.clanladder.service.Orchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClanladderApplication {

    @Value("${google.sheet.id}")
    private String spreadsheetId;

    @Autowired
    private Orchestrator orchestrator;

    @Autowired
    private GoogleSheetsService sheetsService;

    public static void main(String[] args) {
        SpringApplication.run(ClanladderApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runLadderOnStartup() {
        orchestrator.runAll();
        System.exit(0);
//        try {
//            System.out.println("Called runLadderOnStartup");
//            List<List<Object>> rows = sheetsService.readSheet(spreadsheetId, "templates!A2:B101");
//
//            for (List<Object> row : rows) {
//                if (row.isEmpty()) continue;
//
//                String name = row.get(0).toString();
//                String link = row.size() > 1 ? row.get(1).toString() : "";
//                System.out.println(name + " -> " + link);
//            }
//        } catch (Exception e) {
//            System.err.println("❌ Failed to read Google Sheet:");
//            e.printStackTrace();
//        } finally {
//            System.exit(0);
//        }
    }
}

