package com.mhunters.clanladder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClanladderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClanladderApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runLadderOnStartup() {
        System.out.println("Hello World");
        System.exit(0);
        // Replace with ladder logic later
    }


}
