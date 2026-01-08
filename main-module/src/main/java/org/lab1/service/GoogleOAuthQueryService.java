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
public class GoogleOAuthQueryService {
  private static final String GOOGLE_AUTH_METRIC = "auth.google.connect";
  private static final String STATUS_TAG = "status";
  private static final String SUCCESS_TAG = "success";
  private static final String FAIL_TAG = "fail";
  private static final String GOOGLE_TIME_METRIC = "auth.google.time";
  private static final String STATE_PARAM = "state";
  private static final String CLIENT_ID_PARAM = "client_id";
  private static final String RESPONSE_TYPE_PARAM = "response_type";
  private static final String SCOPE_PARAM = "scope";
  private static final String ACCESS_TYPE_PARAM = "access_type";
  private static final String PROMPT_PARAM = "prompt";
  private static final String REDIRECT_URI_PARAM = "redirect_uri";
  private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
  private static final String EMAIL_KEY = "email";
  private static final String EMAIL_VERIFIED_KEY = "email_verified";
  private static final String ACCOUNT_NOT_CONNECTED_ERROR = "Google account not connected";
  private static final String NO_REFRESH_TOKEN_ERROR = "No refresh token available";
  private static final String TOKEN_REFRESH_FAILED = "Token refresh failed: ";
  private static final String REFRESH_TOKEN_FAILED = "Refresh token failed: ";
  private static final String DB_ERROR = "Database error: ";
  private static final String CONTENT_TYPE_HEADER = "Content-Type";
  private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
  private static final String BEARER_AUTH_PREFIX = "Bearer ";
  private static final String CLIENT_SECRET_PARAM = "client_secret";
  private static final String REFRESH_TOKEN_PARAM = "refresh_token";
  private static final String GRANT_TYPE_PARAM = "grant_type";
  private static final String REFRESH_GRANT_TYPE = "refresh_token";
  private static final String EMAIL_NOT_FOUND_ERROR = "Email not found in Google response";
  private static final String EMAIL_NOT_VERIFIED_ERROR = "Google email not verified";
  private static final String GOOGLE_API_STATUS_ERROR = "Google API returned non-success status: ";
  private static final String GOOGLE_API_CONNECT_ERROR = "Failed to connect to Google API: ";
  private static final String UNEXPECTED_EMAIL_ERROR =
      "Unexpected error while fetching user email: ";
  private static final String GOOGLE_API_ERROR = "Google API error: ";

  private static final String GOOGLE_CLIENT_ID = EnvConfig.get("GOOGLE_CLIENT_ID");
  private static final String GOOGLE_CLIENT_SECRET = EnvConfig.get("GOOGLE_CLIENT_SECRET");
  private static final String GOOGLE_TOKEN_URI = EnvConfig.get("GOOGLE_TOKEN_URI");
  private static final String GOOGLE_SCOPES = EnvConfig.get("GOOGLE_SCOPES");
  private static final String GOOGLE_REDIRECT_URI = EnvConfig.get("GOOGLE_REDIRECT_URI");
  private static final String GOOGLE_AUTH_URI = EnvConfig.get("GOOGLE_AUTH_URI");

  private final RestTemplate restTemplate;
  private final OAuthStateService googleStateService;
  private final GoogleAuthDataRepository googleAuthDataRepository;
  private final MeterRegistry meterRegistry;
  private final Counter googleAuthSuccessCounter;
  private final Counter googleAuthFailCounter;
  private final Timer googleAuthTimer;

  @Autowired
  public GoogleOAuthQueryService(
      final RestTemplate restTemplateParam,
      final OAuthStateService googleStateServiceParam,
      final GoogleAuthDataRepository googleAuthDataRepositoryParam,
      final MeterRegistry meterRegistryParam) {
    this.restTemplate = restTemplateParam;
    this.googleStateService = googleStateServiceParam;
    this.googleAuthDataRepository = googleAuthDataRepositoryParam;
    this.meterRegistry = meterRegistryParam;
    this.googleAuthSuccessCounter =
        Counter.builder(GOOGLE_AUTH_METRIC)
            .tag(STATUS_TAG, SUCCESS_TAG)
            .register(meterRegistryParam);
    this.googleAuthFailCounter =
        Counter.builder(GOOGLE_AUTH_METRIC).tag(STATUS_TAG, FAIL_TAG).register(meterRegistryParam);
    this.googleAuthTimer = Timer.builder(GOOGLE_TIME_METRIC).register(meterRegistryParam);
  }

  public final String getAuthorizationUrl(final int userId, final String state) {
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
          GOOGLE_API_ERROR + httpClientException.getStatusCode() + " - " + errorDetails);
    } catch (RestClientException restClientException) {
      throw new OAuthException(GOOGLE_API_CONNECT_ERROR + restClientException.getMessage());
    } catch (Exception exception) {
      throw new OAuthException(UNEXPECTED_EMAIL_ERROR + exception.getMessage());
    }
  }

  public final boolean isGoogleConnected(final int userId) throws OAuthException {
    try {
      GoogleAuthData authData =
          googleAuthDataRepository
              .findByUserId(userId)
              .orElseThrow(() -> new OAuthException(ACCOUNT_NOT_CONNECTED_ERROR));

      if (Instant.now().isBefore(authData.getExpiryDate())) {
        return true;
      }

      if (authData.getRefreshToken() != null) {
        return true;
      }

      googleAuthDataRepository.delete(authData);
      return false;

    } catch (DataAccessException dataAccessException) {
      throw new OAuthException(DB_ERROR + dataAccessException.getMessage());
    }
  }

  public final void refreshAccessToken(final int userId) throws OAuthException {
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
      throw new OAuthException(GOOGLE_API_ERROR + httpClientException.getResponseBodyAsString());

    } catch (DataAccessException dataAccessException) {
      throw new OAuthException(DB_ERROR + dataAccessException.getMessage());

    } catch (Exception exception) {
      throw new OAuthException(TOKEN_REFRESH_FAILED + exception.getMessage());
    }
  }

  public final String getUserGoogleEmail(final int userId) throws OAuthException {
    GoogleAuthData authData =
        googleAuthDataRepository
            .findByUserId(userId)
            .orElseThrow(() -> new OAuthException(ACCOUNT_NOT_CONNECTED_ERROR));
    return authData.getUserEmail();
  }
}
