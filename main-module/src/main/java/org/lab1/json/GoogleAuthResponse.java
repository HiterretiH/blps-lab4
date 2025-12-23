package org.lab1.json;

public final class GoogleAuthResponse {
  private String authUrl;
  private String state;
  private String message;

  public GoogleAuthResponse() {
  }

  public GoogleAuthResponse(
      final String authUrlParam,
      final String stateParam,
      final String messageParam) {
    this.authUrl = authUrlParam;
    this.state = stateParam;
    this.message = messageParam;
  }

  public String getAuthUrl() {
    return authUrl;
  }

  public void setAuthUrl(final String authUrlParam) {
    this.authUrl = authUrlParam;
  }

  public String getState() {
    return state;
  }

  public void setState(final String stateParam) {
    this.state = stateParam;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String messageParam) {
    this.message = messageParam;
  }
}
