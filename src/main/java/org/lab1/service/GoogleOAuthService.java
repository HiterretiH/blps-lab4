package org.lab1.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.lab1.exception.OAuthException;
import org.lab1.json.GoogleTokenResponse;
import org.lab1.model.GoogleAuthData;
import org.lab1.repository.GoogleAuthDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;

@Service
public class GoogleOAuthService {
    private final RestTemplate restTemplate;
    private final OAuthStateService googleStateService;
    private final GoogleAuthDataRepository googleAuthDataRepository;
    private final MeterRegistry meterRegistry;
    private final Counter googleAuthSuccessCounter;
    private final Counter googleAuthFailCounter;
    private final Timer googleAuthTimer;

    @Autowired
    public GoogleOAuthService(RestTemplate restTemplate,
                              OAuthStateService googleStateService,
                              GoogleAuthDataRepository googleAuthDataRepository,
                              MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.googleStateService = googleStateService;
        this.googleAuthDataRepository = googleAuthDataRepository;
        this.meterRegistry = meterRegistry;
        this.googleAuthSuccessCounter = Counter.builder("auth.google.connect")
                .tag("status", "success")
                .register(meterRegistry);

        this.googleAuthFailCounter = Counter.builder("auth.google.connect")
                .tag("status", "fail")
                .register(meterRegistry);

        this.googleAuthTimer = Timer.builder("auth.google.time")
                .register(meterRegistry);
    }

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.auth.uri}")
    private String authUri;

    @Value("${google.token.uri}")
    private String tokenUri;

    @Value("${google.scopes}")
    private String scopes;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    public String getAuthorizationUrl(int userId, String state) {
        googleStateService.storeGoogleAuthState(userId, state);

        return UriComponentsBuilder.fromHttpUrl(authUri)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("scope", scopes)
                .queryParam("access_type", "offline")
                .queryParam("state", state)
                .queryParam("prompt", "consent")
                .queryParam("redirect_uri", redirectUri)
                .build()
                .toUriString();
    }

    public void processGoogleCallback(int userId, String code, String state) throws OAuthException {
        Timer.Sample timer = Timer.start(meterRegistry);

        try {
            if (!googleStateService.validateGoogleAuthState(userId, state)) {
                throw new OAuthException("Invalid state parameter");
            }

            GoogleTokenResponse tokenResponse = exchangeCodeForTokens(code);

            String userEmail = getUserEmail(tokenResponse.getAccessToken());

            saveAuthData(userId, userEmail, tokenResponse);
        }
        catch (OAuthException e){
            googleAuthFailCounter.increment();
        }
        finally{
            timer.stop(googleAuthTimer);
        }
    }

    private GoogleTokenResponse exchangeCodeForTokens(String code) throws OAuthException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String requestBody = UriComponentsBuilder.newInstance()
                    .queryParam("code", code)
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", clientSecret)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("grant_type", "authorization_code")
                    .build()
                    .toString()
                    .substring(1);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                    tokenUri,
                    request,
                    GoogleTokenResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new OAuthException("Google OAuth error: " +
                        (response.getBody() != null ?
                                response.getBody().toString() :
                                "Empty response"));
            }

            googleAuthSuccessCounter.increment();
            return response.getBody();

        } catch (RestClientException e) {
            googleAuthFailCounter.increment();
            throw new OAuthException("Google API request failed: " + e.getMessage());
        } catch (Exception e) {
            googleAuthFailCounter.increment();
            throw new OAuthException("Unexpected error during token exchange");
        }
    }

    private String getUserEmail(String accessToken) throws OAuthException {
        try {
            String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new OAuthException("Google API returned non-success status: " + response.getStatusCode());
            }

            Map<String, Object> responseBody = response.getBody();
            String email = (String) responseBody.get("email");

            if (email == null || email.isEmpty()) {
                throw new OAuthException("Email not found in Google response");
            }

            Boolean emailVerified = (Boolean) responseBody.get("email_verified");
            if (emailVerified == null || !emailVerified) {
                throw new OAuthException("Google email not verified");
            }

            return email;

        } catch (HttpClientErrorException e) {
            String errorDetails = e.getResponseBodyAsString();
            throw new OAuthException("Google API error: " + e.getStatusCode() + " - " + errorDetails);
        } catch (RestClientException e) {
            throw new OAuthException("Failed to connect to Google API: " + e.getMessage());
        } catch (Exception e) {
            throw new OAuthException("Unexpected error while fetching user email: " + e.getMessage());
        }
    }

    private void saveAuthData(int userId, String email, GoogleTokenResponse tokenResponse) throws OAuthException {
        try {
            GoogleAuthData authData = googleAuthDataRepository.findByUserId(userId)
                    .orElse(new GoogleAuthData());

            if (email == null || email.isEmpty()) {
                throw new OAuthException("Email cannot be null or empty");
            }
            authData.setUserEmail(email);

            if (tokenResponse.getAccessToken() == null) {
                throw new OAuthException("Access token cannot be null");
            }
            authData.setAccessToken(tokenResponse.getAccessToken());

            if (tokenResponse.getRefreshToken() != null) {
                authData.setRefreshToken(tokenResponse.getRefreshToken());
            }

            authData.setUserId(userId);
            authData.setExpiryDate(Instant.now().plusSeconds(tokenResponse.getExpiresIn()));
            authData.setTokenType(tokenResponse.getTokenType());
            authData.setScope(tokenResponse.getScope());

            googleAuthDataRepository.save(authData);

        } catch (DataAccessException e) {
            throw new OAuthException("Failed to save auth data to database: " + e.getMessage());
        }
    }

    public boolean isGoogleConnected(int userId) throws OAuthException {
        try {
            GoogleAuthData authData = googleAuthDataRepository.findByUserId(userId)
                    .orElseThrow(() -> new OAuthException("Google account not connected"));

            if (Instant.now().isBefore(authData.getExpiryDate())) {
                return true;
            }

            if (authData.getRefreshToken() != null) {
                try {
                    refreshAccessToken(userId);
                    return true;
                } catch (OAuthException e) {
                    googleAuthDataRepository.delete(authData);
                    throw new OAuthException("Failed to refresh token: " + e.getMessage());
                }
            }

            googleAuthDataRepository.delete(authData);
            return false;

        } catch (DataAccessException e) {
            throw new OAuthException("Database error: " + e.getMessage());
        }
    }

    public void refreshAccessToken(int userId) throws OAuthException {
        try {
            GoogleAuthData authData = googleAuthDataRepository.findByUserId(userId)
                    .orElseThrow(() -> new OAuthException("User not connected with Google"));

            if (authData.getRefreshToken() == null) {
                throw new OAuthException("No refresh token available");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String requestBody = UriComponentsBuilder.newInstance()
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", clientSecret)
                    .queryParam("refresh_token", authData.getRefreshToken())
                    .queryParam("grant_type", "refresh_token")
                    .build()
                    .toString()
                    .substring(1);

            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                    tokenUri,
                    new HttpEntity<>(requestBody, headers),
                    GoogleTokenResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new OAuthException("Refresh token failed: " + response.getStatusCode());
            }

            GoogleTokenResponse tokenResponse = response.getBody();

            authData.setAccessToken(tokenResponse.getAccessToken());
            authData.setExpiryDate(Instant.now().plusSeconds(tokenResponse.getExpiresIn()));
            authData.setTokenType(tokenResponse.getTokenType());
            authData.setScope(tokenResponse.getScope());

            googleAuthDataRepository.save(authData);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                googleAuthDataRepository.deleteByUserId(userId);
            }
            throw new OAuthException("Google API error: " + e.getResponseBodyAsString());

        } catch (DataAccessException e) {
            throw new OAuthException("Database error: " + e.getMessage());

        } catch (Exception e) {
            throw new OAuthException("Token refresh failed: " + e.getMessage());
        }
    }

    public String getUserGoogleEmail(int userId) throws OAuthException {
        GoogleAuthData authData = googleAuthDataRepository.findByUserId(userId)
                .orElseThrow(() -> new OAuthException("Google account not connected"));
        return authData.getUserEmail();
    }
}
