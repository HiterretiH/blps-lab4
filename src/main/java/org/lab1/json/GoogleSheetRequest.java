package org.lab1.json;

public class GoogleSheetRequest {
    private String googleEmail;
    private String sheetTitle;

    // Constructors, getters and setters
    public GoogleSheetRequest() {}

    public GoogleSheetRequest(String googleEmail, String sheetTitle) {
        this.googleEmail = googleEmail;
        this.sheetTitle = sheetTitle;
    }

    public String getGoogleEmail() {
        return googleEmail;
    }

    public void setGoogleEmail(String googleEmail) {
        this.googleEmail = googleEmail;
    }

    public String getSheetTitle() {
        return sheetTitle;
    }

    public void setSheetTitle(String sheetTitle) {
        this.sheetTitle = sheetTitle;
    }
}