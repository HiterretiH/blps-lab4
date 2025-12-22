package org.lab1.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoogleTokenResponse {
  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("expires_in")
  private int expiresIn;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("scope")
  private String scope;

  @JsonProperty("token_type")
  private String tokenType;

  @JsonProperty("refresh_token_expires_in")
  private Integer refreshTokenExpiresIn;

  public String getAccessToken() {
    return accessToken;
  }

  public int getExpiresIn() {
    return expiresIn;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getScope() {
    return scope;
  }

  public String getTokenType() {
    return tokenType;
  }

  public Integer getRefreshTokenExpiresIn() {
    return refreshTokenExpiresIn;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setExpiresIn(int expiresIn) {
    this.expiresIn = expiresIn;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public void setRefreshTokenExpiresIn(Integer refreshTokenExpiresIn) {
    this.refreshTokenExpiresIn = refreshTokenExpiresIn;
  }

  @Override
  public String toString() {
    return "GoogleTokenResponse{"
        + "accessToken='[PROTECTED]'"
        + ", expiresIn="
        + expiresIn
        + ", refreshToken='"
        + (refreshToken != null ? "[PROTECTED]" : "null")
        + '\''
        + ", scope='"
        + scope
        + '\''
        + ", tokenType='"
        + tokenType
        + '\''
        + ", refreshTokenExpiresIn="
        + refreshTokenExpiresIn
        + '}';
  }
}
