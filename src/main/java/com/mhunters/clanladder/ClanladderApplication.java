package com.mhunters.clanladder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClanladderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClanladderApplication.class, args);
    }


}
