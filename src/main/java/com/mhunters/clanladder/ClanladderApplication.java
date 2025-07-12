package com.mhunters.clanladder;

import com.mhunters.clanladder.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
public class ClanladderApplication {

    @Value("${google.sheet.id}")
    private String spreadsheetId;

    @Autowired
    private GoogleSheetsService sheetsService;

    public static void main(String[] args) {
        SpringApplication.run(ClanladderApplication.class, args);
    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void runLadderOnStartup() {
//        System.out.println("Hello World");
//        System.exit(0);
//        // Replace with ladder logic later
//    }

    @EventListener(ApplicationReadyEvent.class)
    public void runLadderOnStartup() {
        try {
            System.out.println("Called runLadderOnStartup");
            List<List<Object>> rows = sheetsService.readSheet(spreadsheetId, "templates!A2:B101");

            for (List<Object> row : rows) {
                if (row.isEmpty()) continue;

                String name = row.get(0).toString();
                String link = row.size() > 1 ? row.get(1).toString() : "";
                System.out.println(name + " -> " + link);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to read Google Sheet:");
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}

