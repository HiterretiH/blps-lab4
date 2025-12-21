package org.lab3.google.service;

import org.lab3.google.json.MonetizationEvent;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface GoogleConnection {
    String uploadFile(String fileName, byte[] fileData) throws IOException;
    void close();

    String createGoogleSheet(String title) throws IOException;
    void updateGoogleSheet(String sheetId, List<List<Object>> data) throws IOException;
    String createGoogleForm(String title, Map<String, String> fields) throws IOException;
    String createRevenueSpreadsheetWithData(String title, List<String> headers, List<List<Object>> data) throws IOException;
    void addAppSheets(String googleEmail, String spreadsheetTitle, String appName) throws IOException;
    void updateMonetizationSheets(MonetizationEvent event) throws IOException;
    void updateAppsTop() throws IOException;
}
