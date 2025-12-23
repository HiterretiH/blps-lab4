package org.lab1.json;

public final class AddAppSheetsRequest {
  private String spreadsheetId;
  private int appId;

  public AddAppSheetsRequest() {
  }

  public AddAppSheetsRequest(final String spreadsheetIdParam, final int appIdParam) {
    this.spreadsheetId = spreadsheetIdParam;
    this.appId = appIdParam;
  }

  public String getSpreadsheetId() {
    return spreadsheetId;
  }

  public void setSpreadsheetId(final String spreadsheetIdParam) {
    this.spreadsheetId = spreadsheetIdParam;
  }

  public int getAppId() {
    return appId;
  }

  public void setAppId(final int appIdParam) {
    this.appId = appIdParam;
  }
}
