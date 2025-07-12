package com.mhunters.clanladder.external;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetGame;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetRanking;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetSignup;
import com.mhunters.clanladder.data.googlesheet.GoogleSheetTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
 
/**
 * This class encapsulates the Google sheet access using Google Sheets API.
 */
@Service
public class GoogleSheetAccess {
    private final Sheets sheetsService;

    @Value("${google.sheet.id}")
    private String spreadsheetId;

    public GoogleSheetAccess(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    public List<GoogleSheetTemplate> readTemplates() throws IOException {
        String range = "Templates!A2:B";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        if (response.getValues() == null) {
            return Collections.emptyList();
        }

        return response.getValues().stream()
                .map(row -> {
                    GoogleSheetTemplate template = new GoogleSheetTemplate();
                    if (row.size() > 0) template.setName(row.get(0).toString());
                    if (row.size() > 1) template.setLink(row.get(1).toString());
                    return template;
                })
                .collect(Collectors.toList());
    }

    public List<GoogleSheetSignup> readSignups() throws IOException {
        String range = "Signup!A2:C";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        if (response.getValues() == null) {
            return Collections.emptyList();
        }

        return response.getValues().stream()
                .map(row -> {
                    GoogleSheetSignup signup = new GoogleSheetSignup();
                    if (row.size() > 0) signup.setName(row.get(0).toString());
                    if (row.size() > 1) signup.setToken(row.get(1).toString());
                    if (row.size() > 2) {
                        try {
                            signup.setMaxGames(Integer.parseInt(row.get(2).toString()));
                        } catch (NumberFormatException e) {
                            signup.setMaxGames(0);
                        }
                    }
                    return signup;
                })
                .collect(Collectors.toList());
    }

    public void deleteRankings() throws IOException {
        String range = "Rankings!A2:Z";
        ClearValuesRequest requestBody = new ClearValuesRequest();
        sheetsService.spreadsheets().values()
                .clear(spreadsheetId, range, requestBody)
                .execute();
    }

    public void insertRankings(List<GoogleSheetRanking> googleSheetRankings) throws IOException {
        if (googleSheetRankings.isEmpty()) return;

        List<List<Object>> values = googleSheetRankings.stream()
                .map(ranking -> {
                    List<Object> row = new ArrayList<>();
                    row.add(ranking.getRank());
                    row.add(ranking.getName());
                    row.add(ranking.getRating());
                    return row;
                })
                .collect(Collectors.toList());

        ValueRange body = new ValueRange()
                .setValues(values);

        String range = "Rankings!A2";
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void insertGameHistory(List<GoogleSheetGame> googleSheetGames) throws IOException {
        if (googleSheetGames.isEmpty()) return;

        List<List<Object>> values = googleSheetGames.stream()
                .map(game -> {
                    List<Object> row = new ArrayList<>();
                    row.add(game.getReportDate());
                    row.add(game.getTemplate());
                    row.add(game.getPlayer1Name());
                    row.add(game.getPlayer2Name());
                    row.add(game.getResult());
                    row.add(game.getLink());
                    return row;
                })
                .collect(Collectors.toList());

        ValueRange body = new ValueRange()
                .setValues(values);

        String range = "Games!A2";
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
    }
}
