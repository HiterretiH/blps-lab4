package org.lab1.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
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
