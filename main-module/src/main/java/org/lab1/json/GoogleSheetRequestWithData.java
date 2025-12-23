package org.lab1.json;

import java.util.List;

public final class GoogleSheetRequestWithData {
  private String googleEmail;
  private String sheetTitle;
  private List<String> headers;
  private List<List<Object>> data;

  public GoogleSheetRequestWithData() {
  }

  public GoogleSheetRequestWithData(
      final String googleEmailParam,
      final String sheetTitleParam,
      final List<String> headersParam,
      final List<List<Object>> dataParam) {
    this.googleEmail = googleEmailParam;
    this.sheetTitle = sheetTitleParam;
    this.headers = headersParam;
    this.data = dataParam;
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

  public List<String> getHeaders() {
    return headers;
  }

  public void setHeaders(final List<String> headersParam) {
    this.headers = headersParam;
  }

  public List<List<Object>> getData() {
    return data;
  }

  public void setData(final List<List<Object>> dataParam) {
    this.data = dataParam;
  }
}
