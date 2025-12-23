package org.lab1.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.Map;
import org.lab1.config.EnvConfig;
import org.lab1.exception.OAuthException;
import org.lab1.json.GoogleTokenResponse;
import org.lab1.model.GoogleAuthData;
import org.lab1.repository.GoogleAuthDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleOAuthService {
  private static final String GOOGLE_AUTH_METRIC = "auth.google.connect";
  private static final String STATUS_TAG = "status";
  private static final String SUCCESS_TAG = "success";
  private static final String FAIL_TAG = "fail";
  private static final String GOOGLE_TIME_METRIC = "auth.google.time";
  private static final String STATE_PARAM = "state";
  private static final String INVALID_STATE_ERROR = "Invalid state parameter";
  private static final String GOOGLE_OAUTH_ERROR = "Google OAuth error: ";
  private static final String EMPTY_RESPONSE = "Empty response";
  private static final String GOOGLE_API_ERROR = "Google API request failed: ";
  private static final String UNEXPECTED_TOKEN_ERROR = "Unexpected error during token exchange";
  private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
  private static final String EMAIL_KEY = "email";
  private static final String EMAIL_VERIFIED_KEY = "email_verified";
  private static final String EMAIL_NOT_FOUND_ERROR = "Email not found in Google response";
  private static final String EMAIL_NOT_VERIFIED_ERROR = "Google email not verified";
  private static final String GOOGLE_API_STATUS_ERROR = "Google API returned non-success status: ";
  private static final String GOOGLE_API_CONNECT_ERROR = "Failed to connect to Google API: ";
  private static final String UNEXPECTED_EMAIL_ERROR = "Unexpected error while fetching user email: ";
  private static final String EMAIL_NULL_ERROR = "Email cannot be null or empty";
  private static final String ACCESS_TOKEN_NULL_ERROR = "Access token cannot be null";
  private static final String DB_SAVE_ERROR = "Failed to save auth data to database: ";
  private static final String ACCOUNT_NOT_CONNECTED_ERROR = "Google account not connected";
  private static final String NO_REFRESH_TOKEN_ERROR = "No refresh token available";
  private static final String REFRESH_TOKEN_FAILED = "Refresh token failed: ";
  private static final String DB_ERROR = "Database error: ";
  private static final String TOKEN_REFRESH_FAILED = "Token refresh failed: ";
  private static final String CLIENT_ID_PARAM = "client_id";
  private static final String RESPONSE_TYPE_PARAM = "response_type";
  private static final String SCOPE_PARAM = "scope";
  private static final String ACCESS_TYPE_PARAM = "access_type";
  private static final String PROMPT_PARAM = "prompt";
  private static final String REDIRECT_URI_PARAM = "redirect_uri";
  private static final String CODE_PARAM = "code";
  private static final String CLIENT_SECRET_PARAM = "client_secret";
  private static final String GRANT_TYPE_PARAM = "grant_type";
  private static final String AUTHORIZATION_CODE = "authorization_code";
  private static final String REFRESH_TOKEN_PARAM = "refresh_token";
  private static final String REFRESH_GRANT_TYPE = "refresh_token";
  private static final String CONTENT_TYPE_HEADER = "Content-Type";
  private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
  private static final String BEARER_AUTH_PREFIX = "Bearer ";

  private static final String GOOGLE_CLIENT_ID = EnvConfig.get("GOOGLE_CLIENT_ID");
  private static final String GOOGLE_CLIENT_SECRET = EnvConfig.get("GOOGLE_CLIENT_SECRET");
  private static final String GOOGLE_AUTH_URI = EnvConfig.get("GOOGLE_AUTH_URI");
  private static final String GOOGLE_TOKEN_URI = EnvConfig.get("GOOGLE_TOKEN_URI");
  private static final String GOOGLE_SCOPES = EnvConfig.get("GOOGLE_SCOPES");
  private static final String GOOGLE_REDIRECT_URI = EnvConfig.get("GOOGLE_REDIRECT_URI");

  private final RestTemplate restTemplate;
  private final OAuthStateService googleStateService;
  private final GoogleAuthDataRepository googleAuthDataRepository;
  private final MeterRegistry meterRegistry;
  private final Counter googleAuthSuccessCounter;
  private final Counter googleAuthFailCounter;
  private final Timer googleAuthTimer;

  @Autowired
  public GoogleOAuthService(
      final RestTemplate restTemplate,
      final OAuthStateService googleStateService,
      final GoogleAuthDataRepository googleAuthDataRepository,
      final MeterRegistry meterRegistry) {
    this.restTemplate = restTemplate;
    this.googleStateService = googleStateService;
    this.googleAuthDataRepository = googleAuthDataRepository;
    this.meterRegistry = meterRegistry;
    this.googleAuthSuccessCounter =
        Counter.builder(GOOGLE_AUTH_METRIC).tag(STATUS_TAG, SUCCESS_TAG).register(meterRegistry);

    this.googleAuthFailCounter =
        Counter.builder(GOOGLE_AUTH_METRIC).tag(STATUS_TAG, FAIL_TAG).register(meterRegistry);

    this.googleAuthTimer = Timer.builder(GOOGLE_TIME_METRIC).register(meterRegistry);
  }

  public String getAuthorizationUrl(final int userId, final String state) {
    googleStateService.storeGoogleAuthState(userId, state);

    return UriComponentsBuilder.fromHttpUrl(GOOGLE_AUTH_URI)
        .queryParam(CLIENT_ID_PARAM, GOOGLE_CLIENT_ID)
        .queryParam(RESPONSE_TYPE_PARAM, "code")
        .queryParam(SCOPE_PARAM, GOOGLE_SCOPES)
        .queryParam(ACCESS_TYPE_PARAM, "offline")
        .queryParam(STATE_PARAM, state)
        .queryParam(PROMPT_PARAM, "consent")
        .queryParam(REDIRECT_URI_PARAM, GOOGLE_REDIRECT_URI)
        .build()
        .toUriString();
  }

  public void processGoogleCallback(final int userId, final String code, final String state) {
    Timer.Sample timer = Timer.start(meterRegistry);

    try {
      if (!googleStateService.validateGoogleAuthState(userId, state)) {
        throw new OAuthException(INVALID_STATE_ERROR);
      }

      GoogleTokenResponse tokenResponse = exchangeCodeForTokens(code);
      String userEmail = getUserEmail(tokenResponse.getAccessToken());
      saveAuthData(userId, userEmail, tokenResponse);
    } catch (OAuthException oAuthException) {
      googleAuthFailCounter.increment();
    } finally {
      timer.stop(googleAuthTimer);
    }
  }

  private GoogleTokenResponse exchangeCodeForTokens(final String code) throws OAuthException {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      String requestBody =
          UriComponentsBuilder.newInstance()
              .queryParam(CODE_PARAM, code)
              .queryParam(CLIENT_ID_PARAM, GOOGLE_CLIENT_ID)
              .queryParam(CLIENT_SECRET_PARAM, GOOGLE_CLIENT_SECRET)
              .queryParam(REDIRECT_URI_PARAM, GOOGLE_REDIRECT_URI)
              .queryParam(GRANT_TYPE_PARAM, AUTHORIZATION_CODE)
              .build()
              .toString()
              .substring(1);

      HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

      ResponseEntity<GoogleTokenResponse> response =
          restTemplate.postForEntity(GOOGLE_TOKEN_URI, request, GoogleTokenResponse.class);

      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        throw new OAuthException(
            GOOGLE_OAUTH_ERROR
                + (response.getBody() != null ? response.getBody().toString() : EMPTY_RESPONSE));
      }

      googleAuthSuccessCounter.increment();
      return response.getBody();

    } catch (RestClientException restClientException) {
      googleAuthFailCounter.increment();
      throw new OAuthException(GOOGLE_API_ERROR + restClientException.getMessage());
    } catch (Exception exception) {
      googleAuthFailCounter.increment();
      throw new OAuthException(UNEXPECTED_TOKEN_ERROR);
    }
  }

  private String getUserEmail(final String accessToken) throws OAuthException {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              USER_INFO_URL, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {});

      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        throw new OAuthException(GOOGLE_API_STATUS_ERROR + response.getStatusCode());
      }

      Map<String, Object> responseBody = response.getBody();
      String email = (String) responseBody.get(EMAIL_KEY);

      if (email == null || email.isEmpty()) {
        throw new OAuthException(EMAIL_NOT_FOUND_ERROR);
      }

      Boolean emailVerified = (Boolean) responseBody.get(EMAIL_VERIFIED_KEY);
      if (emailVerified == null || !emailVerified) {
        throw new OAuthException(EMAIL_NOT_VERIFIED_ERROR);
      }

      return email;

    } catch (HttpClientErrorException httpClientException) {
      String errorDetails = httpClientException.getResponseBodyAsString();
      throw new OAuthException(
          "Google API error: " + httpClientException.getStatusCode() + " - " + errorDetails);
    } catch (RestClientException restClientException) {
      throw new OAuthException(GOOGLE_API_CONNECT_ERROR + restClientException.getMessage());
    } catch (Exception exception) {
      throw new OAuthException(UNEXPECTED_EMAIL_ERROR + exception.getMessage());
    }
  }

  private void saveAuthData(final int userId, final String email, final GoogleTokenResponse tokenResponse)
      throws OAuthException {
    try {
      GoogleAuthData authData =
          googleAuthDataRepository.findByUserId(userId).orElse(new GoogleAuthData());

      if (email == null || email.isEmpty()) {
        throw new OAuthException(EMAIL_NULL_ERROR);
      }
      authData.setUserEmail(email);

      if (tokenResponse.getAccessToken() == null) {
        throw new OAuthException(ACCESS_TOKEN_NULL_ERROR);
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

    } catch (DataAccessException dataAccessException) {
      throw new OAuthException(DB_SAVE_ERROR + dataAccessException.getMessage());
    }
  }

  public boolean isGoogleConnected(final int userId) throws OAuthException {
    try {
      GoogleAuthData authData =
          googleAuthDataRepository
              .findByUserId(userId)
              .orElseThrow(() -> new OAuthException(ACCOUNT_NOT_CONNECTED_ERROR));

      if (Instant.now().isBefore(authData.getExpiryDate())) {
        return true;
      }

      if (authData.getRefreshToken() != null) {
        try {
          refreshAccessToken(userId);
          return true;
        } catch (OAuthException oAuthException) {
          googleAuthDataRepository.delete(authData);
          throw new OAuthException("Failed to refresh token: " + oAuthException.getMessage());
        }
      }

      googleAuthDataRepository.delete(authData);
      return false;

    } catch (DataAccessException dataAccessException) {
      throw new OAuthException(DB_ERROR + dataAccessException.getMessage());
    }
  }

  public void refreshAccessToken(final int userId) throws OAuthException {
    try {
      GoogleAuthData authData =
          googleAuthDataRepository
              .findByUserId(userId)
              .orElseThrow(() -> new OAuthException("User not connected with Google"));

      if (authData.getRefreshToken() == null) {
        throw new OAuthException(NO_REFRESH_TOKEN_ERROR);
      }

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      String requestBody =
          UriComponentsBuilder.newInstance()
              .queryParam(CLIENT_ID_PARAM, GOOGLE_CLIENT_ID)
              .queryParam(CLIENT_SECRET_PARAM, GOOGLE_CLIENT_SECRET)
              .queryParam(REFRESH_TOKEN_PARAM, authData.getRefreshToken())
              .queryParam(GRANT_TYPE_PARAM, REFRESH_GRANT_TYPE)
              .build()
              .toString()
              .substring(1);

      ResponseEntity<GoogleTokenResponse> response =
          restTemplate.postForEntity(
              GOOGLE_TOKEN_URI, new HttpEntity<>(requestBody, headers), GoogleTokenResponse.class);

      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        throw new OAuthException(REFRESH_TOKEN_FAILED + response.getStatusCode());
      }

      GoogleTokenResponse tokenResponse = response.getBody();

      authData.setAccessToken(tokenResponse.getAccessToken());
      authData.setExpiryDate(Instant.now().plusSeconds(tokenResponse.getExpiresIn()));
      authData.setTokenType(tokenResponse.getTokenType());
      authData.setScope(tokenResponse.getScope());

      googleAuthDataRepository.save(authData);

    } catch (HttpClientErrorException httpClientException) {
      if (httpClientException.getStatusCode() == HttpStatus.BAD_REQUEST) {
        googleAuthDataRepository.deleteByUserId(userId);
      }
      throw new OAuthException(
          "Google API error: " + httpClientException.getResponseBodyAsString());

    } catch (DataAccessException dataAccessException) {
      throw new OAuthException(DB_ERROR + dataAccessException.getMessage());

    } catch (Exception exception) {
      throw new OAuthException(TOKEN_REFRESH_FAILED + exception.getMessage());
    }
  }

  public String getUserGoogleEmail(final int userId) throws OAuthException {
    GoogleAuthData authData =
        googleAuthDataRepository
            .findByUserId(userId)
            .orElseThrow(() -> new OAuthException(ACCOUNT_NOT_CONNECTED_ERROR));
    return authData.getUserEmail();
  }
}
