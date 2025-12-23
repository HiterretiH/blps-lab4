package org.lab1.json;

public final class GoogleSheetIdentifier {
  private String googleEmail;
  private String spreadsheetTitle;

  public GoogleSheetIdentifier() {}

  public GoogleSheetIdentifier(final String googleEmailParam, final String spreadsheetTitleParam) {
    this.googleEmail = googleEmailParam;
    this.spreadsheetTitle = spreadsheetTitleParam;
  }

  public String getGoogleEmail() {
    return googleEmail;
  }

  public void setGoogleEmail(final String googleEmailParam) {
    this.googleEmail = googleEmailParam;
  }

  public String getSpreadsheetTitle() {
    return spreadsheetTitle;
  }

  public void setSpreadsheetTitle(final String spreadsheetTitleParam) {
    this.spreadsheetTitle = spreadsheetTitleParam;
  }
}
