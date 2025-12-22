package org.lab3.google.json;

import java.util.List;

public class GoogleSheetRequestWithData {
  private String googleEmail;
  private String sheetTitle;
  private List<String> headers;
  private List<List<Object>> data;

  public GoogleSheetRequestWithData() {}

  public GoogleSheetRequestWithData(
      String googleEmail, String sheetTitle, List<String> headers, List<List<Object>> data) {
    this.googleEmail = googleEmail;
    this.sheetTitle = sheetTitle;
    this.headers = headers;
    this.data = data;
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

  public List<String> getHeaders() {
    return headers;
  }

  public void setHeaders(List<String> headers) {
    this.headers = headers;
  }

  public List<List<Object>> getData() {
    return data;
  }

  public void setData(List<List<Object>> data) {
    this.data = data;
  }
}
