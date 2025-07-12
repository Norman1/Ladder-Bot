package com.mhunters.clanladder.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleSheetsService {

    private final Sheets sheets;

    public GoogleSheetsService(Sheets sheets) {
        this.sheets = sheets;
    }

    public List<List<Object>> readSheet(String spreadsheetId, String range) {
        ValueRange response = null;
        try {
            response = sheets.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response.getValues() == null ? new ArrayList<>() : response.getValues();
    }

    public void writeToSheet(String spreadsheetId, String range, List<List<Object>> values) throws IOException {
        ValueRange body = new ValueRange().setValues(values);

        sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void clearSheet(String spreadsheetId, String range) throws IOException {
        sheets.spreadsheets().values()
                .clear(spreadsheetId, range, new com.google.api.services.sheets.v4.model.ClearValuesRequest())
                .execute();
    }
}
