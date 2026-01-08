package org.lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "google_auth_data")
@Data
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
}
