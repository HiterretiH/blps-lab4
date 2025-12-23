package org.lab1.json;

public final class GoogleAuthRequest {
  private String code;
  private String state;

  public String getCode() {
    return code;
  }

  public void setCode(final String codeParam) {
    this.code = codeParam;
  }

  public String getState() {
    return state;
  }

  public void setState(final String stateParam) {
    this.state = stateParam;
  }
}
