package com.mhunters.clanladder.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleSheetsConfig {

    private static final String APPLICATION_NAME = "Clan Ladder";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    @Value("${google.credentials.file}")
    private String credentialsFilePath;


    @Bean
    public Sheets getSheetsService() throws IOException, GeneralSecurityException {
        System.out.println("🔧 Loading credentials from: " + credentialsFilePath);

        File file = new File(credentialsFilePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Credentials file not found at: " + credentialsFilePath);
        }

        try (InputStream credentialsStream = new FileInputStream(file)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(SCOPES);

            return new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName(APPLICATION_NAME).build();
        }
    }
}