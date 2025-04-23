package org.lab1.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "google_auth_data")
public class GoogleAuthData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "access_token", nullable = false, length = 2048)
    private String accessToken;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Column(name = "token_type")
    private String tokenType;

    @Column(name = "scope")
    private String scope;

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}