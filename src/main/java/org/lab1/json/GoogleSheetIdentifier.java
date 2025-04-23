package org.lab1.json;

public class GoogleSheetIdentifier {
    private String googleEmail;
    private String spreadsheetTitle;

    public GoogleSheetIdentifier() {}

    public GoogleSheetIdentifier(String googleEmail, String spreadsheetTitle) {
        this.googleEmail = googleEmail;
        this.spreadsheetTitle = spreadsheetTitle;
    }

    public String getGoogleEmail() {
        return googleEmail;
    }

    public void setGoogleEmail(String googleEmail) {
        this.googleEmail = googleEmail;
    }

    public String getSpreadsheetTitle() {
        return spreadsheetTitle;
    }

    public void setSpreadsheetTitle(String spreadsheetTitle) {
        this.spreadsheetTitle = spreadsheetTitle;
    }
}
