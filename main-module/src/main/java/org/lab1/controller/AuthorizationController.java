package org.lab1.controller;

import io.jsonwebtoken.Claims;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.UUID;
import org.lab.logger.Logger;
import org.lab1.exception.ForbiddenException;
import org.lab1.exception.UnauthorizedException;
import org.lab1.exception.ValidationException;
import org.lab1.json.Credentials;
import org.lab1.json.GoogleAuthResponse;
import org.lab1.json.LoginCredentials;
import org.lab1.json.Token;
import org.lab1.model.Role;
import org.lab1.model.User;
import org.lab1.security.TokenManager;
import org.lab1.service.DeveloperService;
import org.lab1.service.GoogleOAuthQueryService;
import org.lab1.service.GoogleOAuthRegistrationService;
import org.lab1.service.UserQueryService;
import org.lab1.service.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthorizationController {
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String LOGIN_REQUEST_LOG = "Received login request for user: ";
  private static final String LOGIN_SUCCESS_LOG = "User ";
  private static final String LOGGED_IN_SUCCESS_LOG = " logged in successfully.";
  private static final String LOGIN_FAILED_LOG = "Login failed for user: ";
  private static final String REASON_LOG = ". Reason: ";
  private static final String REGISTER_REQUEST_LOG = "Received registration request for user: ";
  private static final String REGISTER_SUCCESS_LOG = "User ";
  private static final String REGISTERED_SUCCESS_LOG = " registered successfully.";
  private static final String REGISTER_FAILED_LOG = "Registration failed for user: ";
  private static final String DEVELOPER_REGISTER_REQUEST_LOG =
      "Received developer registration request for user: ";
  private static final String DEVELOPER_REGISTER_SUCCESS_LOG = "Developer ";
  private static final String DEVELOPER_REGISTER_FAILED_LOG =
      "Developer registration failed for user: ";
  private static final String GOOGLE_CONNECT_INITIATE_LOG =
      "Received request to initiate Google connect.";
  private static final String INVALID_TOKEN_LOG =
      "Invalid system token received for Google connect initiation.";
  private static final String INVALID_TOKEN_MESSAGE = "Invalid token";
  private static final String GENERATED_GOOGLE_AUTH_LOG = "Generated Google auth URL for user ID: ";
  private static final String STATE_LOG = ", state: ";
  private static final String SECURITY_EXCEPTION_LOG =
      "Security exception during Google connect initiation: ";
  private static final String GOOGLE_CALLBACK_LOG = "Received Google connect callback with code: ";
  private static final String INVALID_CALLBACK_TOKEN_LOG =
      "Invalid system token received for Google connect callback.";
  private static final String GOOGLE_ACCOUNT_CONNECTED_LOG =
      "Google account connected for user ID: ";
  private static final String CONNECT_SUCCESS_MESSAGE =
      "Google account connected and email saved successfully!";
  private static final String GOOGLE_OAUTH_ERROR_LOG = "Google OAuth error during callback: ";
  private static final String INTERNAL_SERVER_ERROR_LOG =
      "Internal server error during Google connect callback: ";
  private static final String AUTH_ERROR_METRIC = "auth.error";
  private static final String TYPE_LABEL = "type";
  private static final String LOGIN_TYPE = "login";
  private static final String AUTH_GOOGLE_ERROR_METRIC = "auth.google.error";
  private static final String REASON_LABEL = "reason";
  private static final String USER_ID_CLAIM = "userId";

  @Autowired private UserQueryService userQueryService;
  @Autowired private UserRegistrationService userRegistrationService;
  @Autowired private DeveloperService developerService;
  @Autowired private PlatformTransactionManager transactionManager;
  @Autowired private GoogleOAuthQueryService googleOAuthQueryService;
  @Autowired private GoogleOAuthRegistrationService googleOAuthRegistrationService;
  @Autowired private TokenManager tokenManager;
  @Autowired private MeterRegistry meterRegistry;

  @Autowired
  @Qualifier("correlationLogger")
  private Logger logger;

  @PostMapping("/login")
  public ResponseEntity<Token> login(@RequestBody final LoginCredentials credentials) {
    logger.info(LOGIN_REQUEST_LOG + credentials.getUsername());
    try {
      Token token =
          userQueryService.authenticateUser(credentials.getUsername(), credentials.getPassword());
      logger.info(LOGIN_SUCCESS_LOG + credentials.getUsername() + LOGGED_IN_SUCCESS_LOG);
      return ResponseEntity.status(HttpStatus.CREATED).body(token);
    } catch (IllegalArgumentException exception) {
      meterRegistry.counter(AUTH_ERROR_METRIC, TYPE_LABEL, LOGIN_TYPE).increment();
      logger.error(
          LOGIN_FAILED_LOG + credentials.getUsername() + REASON_LOG + exception.getMessage());
      throw new UnauthorizedException("Invalid username or password");
    }
  }

  @PostMapping("/register")
  public ResponseEntity<Token> registerUser(@RequestBody final Credentials credentials) {
    logger.info(REGISTER_REQUEST_LOG + credentials.getUsername());
    try {
      Token token =
          userRegistrationService.registerUserAndGetToken(
              credentials.getUsername(),
              credentials.getEmail(),
              credentials.getPassword(),
              Role.USER);
      logger.info(REGISTER_SUCCESS_LOG + credentials.getUsername() + REGISTERED_SUCCESS_LOG);
      return ResponseEntity.status(HttpStatus.CREATED).body(token);
    } catch (IllegalArgumentException exception) {
      logger.error(
          REGISTER_FAILED_LOG + credentials.getUsername() + REASON_LOG + exception.getMessage());
      throw new ValidationException(exception.getMessage());
    }
  }

  @PostMapping("/developer/register")
  public ResponseEntity<Token> registerDeveloper(@RequestBody final Credentials credentials) {
    logger.info(DEVELOPER_REGISTER_REQUEST_LOG + credentials.getUsername());
    TransactionDefinition definition = new DefaultTransactionDefinition();
    TransactionStatus status = transactionManager.getTransaction(definition);

    try {
      User user =
          userRegistrationService.registerUser(
              credentials.getUsername(),
              credentials.getEmail(),
              credentials.getPassword(),
              Role.DEVELOPER);
      Token token = userRegistrationService.generateToken(user);
      developerService.createDeveloper(user);
      transactionManager.commit(status);
      logger.info(
          DEVELOPER_REGISTER_SUCCESS_LOG + credentials.getUsername() + REGISTERED_SUCCESS_LOG);
      return ResponseEntity.status(HttpStatus.CREATED).body(token);
    } catch (IllegalArgumentException exception) {
      transactionManager.rollback(status);
      logger.error(
          DEVELOPER_REGISTER_FAILED_LOG
              + credentials.getUsername()
              + REASON_LOG
              + exception.getMessage());
      throw new ValidationException(exception.getMessage());
    }
  }

  @PostMapping("/google/connect")
  public ResponseEntity<GoogleAuthResponse> initiateGoogleConnect(
      @RequestHeader("Authorization") final String authHeader) {
    logger.info(GOOGLE_CONNECT_INITIATE_LOG);
    try {
      String systemToken = authHeader.replace(BEARER_PREFIX, "");

      if (!tokenManager.isTokenValid(systemToken)) {
        logger.error(INVALID_TOKEN_LOG);
        throw new ForbiddenException(INVALID_TOKEN_MESSAGE);
      }

      Claims claims = tokenManager.getClaimsFromToken(systemToken);
      Integer userId = (Integer) claims.get(USER_ID_CLAIM);
      String state = UUID.randomUUID().toString();
      String authUrl = googleOAuthQueryService.getAuthorizationUrl(userId, state);
      logger.info(GENERATED_GOOGLE_AUTH_LOG + userId + STATE_LOG + state);

      return ResponseEntity.ok(
          new GoogleAuthResponse(
              authUrl, state, "Copy this URL and open in browser to connect Google account"));
    } catch (SecurityException exception) {
      logger.error(SECURITY_EXCEPTION_LOG + exception.getMessage());
      throw new ForbiddenException(exception.getMessage());
    }
  }

  @PostMapping("/google/connect/callback")
  public ResponseEntity<String> handleGoogleCallback(
      @RequestParam final String code,
      @RequestParam final String state,
      @RequestHeader("Authorization") final String authHeader) {
    logger.info(GOOGLE_CALLBACK_LOG + code + STATE_LOG + state);
    try {
      String systemToken = authHeader.replace(BEARER_PREFIX, "");

      if (!tokenManager.isTokenValid(systemToken)) {
        logger.error(INVALID_CALLBACK_TOKEN_LOG);
        throw new ForbiddenException(INVALID_TOKEN_MESSAGE);
      }

      Claims claims = tokenManager.getClaimsFromToken(systemToken);
      Integer userId = (Integer) claims.get(USER_ID_CLAIM);
      googleOAuthRegistrationService.processGoogleCallback(userId, code, state);
      logger.info(GOOGLE_ACCOUNT_CONNECTED_LOG + userId + ".");
      return ResponseEntity.ok(CONNECT_SUCCESS_MESSAGE);
    } catch (SecurityException exception) {
      meterRegistry
          .counter(AUTH_GOOGLE_ERROR_METRIC, REASON_LABEL, exception.getMessage())
          .increment();
      logger.error(SECURITY_EXCEPTION_LOG + exception.getMessage());
      throw new ForbiddenException(exception.getMessage());
    } catch (Exception exception) {
      meterRegistry
          .counter(AUTH_GOOGLE_ERROR_METRIC, REASON_LABEL, exception.getMessage())
          .increment();
      logger.error(INTERNAL_SERVER_ERROR_LOG + exception.getMessage());
      throw new RuntimeException("Internal server error during Google connect callback");
    }
  }
}
