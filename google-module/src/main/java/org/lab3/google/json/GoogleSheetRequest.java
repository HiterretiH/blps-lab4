package org.lab3.google.json;

public final class GoogleSheetRequest {
  private String googleEmail;
  private String sheetTitle;

  public GoogleSheetRequest() {
  }

  public GoogleSheetRequest(final String googleEmailParam,
                            final String sheetTitleParam) {
    this.googleEmail = googleEmailParam;
    this.sheetTitle = sheetTitleParam;
  }

  public String getGoogleEmail() {
    return googleEmail;
  }

  public void setGoogleEmail(final String googleEmailParam) {
    this.googleEmail = googleEmailParam;
  }

  public String getSheetTitle() {
    return sheetTitle;
  }

  public void setSheetTitle(final String sheetTitleParam) {
    this.sheetTitle = sheetTitleParam;
  }
}
