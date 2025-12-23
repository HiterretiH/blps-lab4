package org.lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "google_auth_data")
public class GoogleAuthData {
  private static final int ACCESS_TOKEN_MAX_LENGTH = 2048;
  private static final int REFRESH_TOKEN_MAX_LENGTH = 512;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Integer userId;

  @Column(name = "user_email", nullable = false)
  private String userEmail;

  @Column(name = "access_token", nullable = false, length = ACCESS_TOKEN_MAX_LENGTH)
  private String accessToken;

  @Column(name = "refresh_token", length = REFRESH_TOKEN_MAX_LENGTH)
  private String refreshToken;

  @Column(name = "expiry_date")
  private Instant expiryDate;

  @Column(name = "token_type")
  private String tokenType;

  @Column(name = "scope")
  private String scope;

  public final Integer getId() {
    return id;
  }

  public final Integer getUserId() {
    return userId;
  }

  public final void setUserId(final Integer userIdParam) {
    this.userId = userIdParam;
  }

  public final String getUserEmail() {
    return userEmail;
  }

  public final void setUserEmail(final String userEmailParam) {
    this.userEmail = userEmailParam;
  }

  public final String getAccessToken() {
    return accessToken;
  }

  public final void setAccessToken(final String accessTokenParam) {
    this.accessToken = accessTokenParam;
  }

  public final String getRefreshToken() {
    return refreshToken;
  }

  public final void setRefreshToken(final String refreshTokenParam) {
    this.refreshToken = refreshTokenParam;
  }

  public final Instant getExpiryDate() {
    return expiryDate;
  }

  public final void setExpiryDate(final Instant expiryDateParam) {
    this.expiryDate = expiryDateParam;
  }

  public final String getTokenType() {
    return tokenType;
  }

  public final void setTokenType(final String tokenTypeParam) {
    this.tokenType = tokenTypeParam;
  }

  public final String getScope() {
    return scope;
  }

  public final void setScope(final String scopeParam) {
    this.scope = scopeParam;
  }
}
