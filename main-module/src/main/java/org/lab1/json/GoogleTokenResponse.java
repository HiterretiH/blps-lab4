package org.lab1.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class GoogleTokenResponse {
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

  public void setAccessToken(final String accessTokenParam) {
    this.accessToken = accessTokenParam;
  }

  public void setExpiresIn(final int expiresInParam) {
    this.expiresIn = expiresInParam;
  }

  public void setRefreshToken(final String refreshTokenParam) {
    this.refreshToken = refreshTokenParam;
  }

  public void setScope(final String scopeParam) {
    this.scope = scopeParam;
  }

  public void setTokenType(final String tokenTypeParam) {
    this.tokenType = tokenTypeParam;
  }

  public void setRefreshTokenExpiresIn(final Integer refreshTokenExpiresInParam) {
    this.refreshTokenExpiresIn = refreshTokenExpiresInParam;
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
