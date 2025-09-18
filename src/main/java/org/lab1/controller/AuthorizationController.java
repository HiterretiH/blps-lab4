package org.lab1.controller;

import io.jsonwebtoken.Claims;
import io.micrometer.core.instrument.MeterRegistry;
import org.lab.logger.Logger;
import org.lab1.exception.OAuthException;
import org.lab1.json.*;
import org.lab1.model.Role;
import org.lab1.model.User;
import org.lab1.security.TokenManager;
import org.lab1.service.DeveloperService;
import org.lab1.service.GoogleOAuthService;
import org.lab1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthorizationController {
    private final UserService userService;
    private final DeveloperService developerService;
    private final PlatformTransactionManager transactionManager;
    private final GoogleOAuthService googleOAuthService;
    private final TokenManager tokenManager;
    private final MeterRegistry meterRegistry;
    private final Logger logger;

    @Autowired
    public AuthorizationController(UserService userService, DeveloperService developerService,
                                   PlatformTransactionManager transactionManager,
                                   GoogleOAuthService googleOAuthService,
                                   TokenManager tokenManager,
                                   MeterRegistry meterRegistry,
                                   Logger logger) {
        this.userService = userService;
        this.developerService = developerService;
        this.transactionManager = transactionManager;
        this.googleOAuthService = googleOAuthService;
        this.tokenManager = tokenManager;
        this.meterRegistry = meterRegistry;
        this.logger = logger;
    }

    @PostMapping("/login")
    public ResponseEntity<Token> login(@RequestBody LoginCredentials credentials) {
        logger.info("Received login request for user: " + credentials.getUsername());
        try {
            Token token = userService.authenticateUser(
                    credentials.getUsername(),
                    credentials.getPassword()
            );
            logger.info("User " + credentials.getUsername() + " logged in successfully.");
            return ResponseEntity.status(HttpStatus.CREATED).body(token);
        } catch (IllegalArgumentException e) {
            meterRegistry.counter("auth.error", "type", "login").increment();
            logger.error("Login failed for user: " + credentials.getUsername() + ". Reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Token> registerUser(@RequestBody Credentials credentials) {
        logger.info("Received registration request for user: " + credentials.getUsername());
        try {
            Token token = userService.registerUserAndGetToken(
                    credentials.getUsername(),
                    credentials.getEmail(),
                    credentials.getPassword(),
                    Role.USER
            );
            logger.info("User " + credentials.getUsername() + " registered successfully.");
            return ResponseEntity.status(HttpStatus.CREATED).body(token);
        } catch (IllegalArgumentException e) {
            logger.error("Registration failed for user: " + credentials.getUsername() + ". Reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/developer/register")
    public ResponseEntity<Token> registerDeveloper(@RequestBody Credentials credentials) {
        logger.info("Received developer registration request for user: " + credentials.getUsername());
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            User user = userService.registerUser(
                    credentials.getUsername(),
                    credentials.getEmail(),
                    credentials.getPassword(),
                    Role.DEVELOPER
            );
            Token token = userService.generateToken(user);
            developerService.createDeveloper(user);
            transactionManager.commit(status);
            logger.info("Developer " + credentials.getUsername() + " registered successfully.");
            return ResponseEntity.status(HttpStatus.CREATED).body(token);
        } catch (IllegalArgumentException e) {
            transactionManager.rollback(status);
            logger.error("Developer registration failed for user: " + credentials.getUsername() + ". Reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/google/connect")
    public ResponseEntity<GoogleAuthResponse> initiateGoogleConnect(
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Received request to initiate Google connect.");
        try {
            String systemToken = authHeader.replace("Bearer ", "");
            if (!tokenManager.isTokenValid(systemToken)) {
                logger.error("Invalid system token received for Google connect initiation.");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }
            Claims c = tokenManager.getClaimsFromToken(systemToken);
            Integer userId = (Integer) c.get("userId");
            String state = UUID.randomUUID().toString();
            String authUrl = googleOAuthService.getAuthorizationUrl(userId, state);
            logger.info("Generated Google auth URL for user ID: " + userId + ", state: " + state);
            return ResponseEntity.ok(
                    new GoogleAuthResponse(
                            authUrl,
                            state,
                            "Copy this URL and open in browser to connect Google account"
                    )
            );
        } catch (SecurityException e) {
            logger.error("Security exception during Google connect initiation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/google/connect/callback")
    public ResponseEntity<String> handleGoogleCallback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Received Google connect callback with code: " + code + ", state: " + state);
        try {
            String systemToken = authHeader.replace("Bearer ", "");
            if (!tokenManager.isTokenValid(systemToken)) {
                logger.error("Invalid system token received for Google connect callback.");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }
            Claims c = tokenManager.getClaimsFromToken(systemToken);
            Integer userId = (Integer) c.get("userId");
            googleOAuthService.processGoogleCallback(userId, code, state);
            logger.info("Google account connected for user ID: " + userId + ".");
            return ResponseEntity.ok("Google account connected and email saved successfully!");
        } catch (SecurityException e) {
            meterRegistry.counter("auth.google.error", "reason", e.getMessage()).increment();
            logger.error("Security exception during Google connect callback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (OAuthException e) {
            meterRegistry.counter("auth.google.error", "reason", e.getMessage()).increment();
            logger.error("Google OAuth error during callback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            meterRegistry.counter("auth.google.error", "reason", e.getMessage()).increment();
            logger.error("Internal server error during Google connect callback: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}