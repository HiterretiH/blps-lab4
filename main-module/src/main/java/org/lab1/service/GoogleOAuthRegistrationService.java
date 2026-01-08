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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleOAuthRegistrationService {
  private static final String GOOGLE_AUTH_METRIC = "auth.google.connect";
  private static final String STATUS_TAG = "status";
  private static final String SUCCESS_TAG = "success";
  private static final String FAIL_TAG = "fail";
  private static final String GOOGLE_TIME_METRIC = "auth.google.time";
  private static final String INVALID_STATE_ERROR = "Invalid state parameter";
  private static final String GOOGLE_OAUTH_ERROR = "Google OAuth error: ";
  private static final String EMPTY_RESPONSE = "Empty response";
  private static final String GOOGLE_API_ERROR = "Google API request failed: ";
  private static final String UNEXPECTED_TOKEN_ERROR = "Unexpected error during token exchange";
  private static final String EMAIL_NULL_ERROR = "Email cannot be null or empty";
  private static final String ACCESS_TOKEN_NULL_ERROR = "Access token cannot be null";
  private static final String DB_SAVE_ERROR = "Failed to save auth data to database: ";
  private static final String CODE_PARAM = "code";
  private static final String CLIENT_ID_PARAM = "client_id";
  private static final String CLIENT_SECRET_PARAM = "client_secret";
  private static final String REDIRECT_URI_PARAM = "redirect_uri";
  private static final String GRANT_TYPE_PARAM = "grant_type";
  private static final String AUTHORIZATION_CODE = "authorization_code";

  private static final String GOOGLE_CLIENT_ID = EnvConfig.get("GOOGLE_CLIENT_ID");
  private static final String GOOGLE_CLIENT_SECRET = EnvConfig.get("GOOGLE_CLIENT_SECRET");
  private static final String GOOGLE_TOKEN_URI = EnvConfig.get("GOOGLE_TOKEN_URI");
  private static final String GOOGLE_REDIRECT_URI = EnvConfig.get("GOOGLE_REDIRECT_URI");

  private final RestTemplate restTemplate;
  private final OAuthStateService googleStateService;
  private final GoogleAuthDataRepository googleAuthDataRepository;
  private final MeterRegistry meterRegistry;
  private final Counter googleAuthSuccessCounter;
  private final Counter googleAuthFailCounter;
  private final Timer googleAuthTimer;

  @Autowired
  public GoogleOAuthRegistrationService(
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

  public final void processGoogleCallback(final int userId, final String code, final String state) {
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
              "https://www.googleapis.com/oauth2/v3/userinfo",
              HttpMethod.GET,
              requestEntity,
              new ParameterizedTypeReference<>() {});

      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        throw new OAuthException(
            "Google API returned non-success status: " + response.getStatusCode());
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

    } catch (Exception exception) {
      throw new OAuthException(
          "Unexpected error while fetching user email: " + exception.getMessage());
    }
  }

  private void saveAuthData(
      final int userId, final String email, final GoogleTokenResponse tokenResponse)
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
}
