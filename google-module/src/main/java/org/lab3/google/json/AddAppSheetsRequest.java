package org.lab3.google.json;

public class AddAppSheetsRequest {
  private String spreadsheetId;
  private int appId;

  public AddAppSheetsRequest() {}

  public AddAppSheetsRequest(String spreadsheetId, int appId) {
    this.spreadsheetId = spreadsheetId;
    this.appId = appId;
  }

  public String getSpreadsheetId() {
    return spreadsheetId;
  }

  public void setSpreadsheetId(String spreadsheetId) {
    this.spreadsheetId = spreadsheetId;
  }

  public int getAppId() {
    return appId;
  }

  public void setAppId(int appId) {
    this.appId = appId;
  }
}
