package org.lab1.controller;

import io.jsonwebtoken.Claims;
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

    @Autowired
    public AuthorizationController(UserService userService, DeveloperService developerService,
                                   PlatformTransactionManager transactionManager,
                                   GoogleOAuthService googleOAuthService,
                                   TokenManager tokenManager) {
        this.userService = userService;
        this.developerService = developerService;
        this.transactionManager = transactionManager;
        this.googleOAuthService = googleOAuthService;
        this.tokenManager = tokenManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Token> login(@RequestBody LoginCredentials credentials) {
        try {
            Token token = userService.authenticateUser(
                    credentials.getUsername(),
                    credentials.getPassword()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(token);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Token> registerUser(@RequestBody Credentials credentials) {
        try {
            Token token = userService.registerUserAndGetToken(
                    credentials.getUsername(),
                    credentials.getEmail(),
                    credentials.getPassword(),
                    Role.USER
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/developer/register")
    public ResponseEntity<Token> registerDeveloper(@RequestBody Credentials credentials) {
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
            return ResponseEntity.status(HttpStatus.CREATED).body(token);
        } catch (IllegalArgumentException e) {
            transactionManager.rollback(status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/google/connect")
    public ResponseEntity<GoogleAuthResponse> initiateGoogleConnect(
            @RequestHeader("Authorization") String authHeader) {

        try {
            String systemToken = authHeader.replace("Bearer ", "");

            if (!tokenManager.isTokenValid(systemToken)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }

            Claims c = tokenManager.getClaimsFromToken(systemToken);

            Integer userId = (Integer) c.get("userId");
            String state = UUID.randomUUID().toString();

            String authUrl = googleOAuthService.getAuthorizationUrl(userId, state);

            return ResponseEntity.ok(
                    new GoogleAuthResponse(
                            authUrl,
                            state,
                            "Copy this URL and open in browser to connect Google account"
                    )
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/google/connect/callback")
    public ResponseEntity<String> handleGoogleCallback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String systemToken = authHeader.replace("Bearer ", "");
            if (!tokenManager.isTokenValid(systemToken)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }

            Claims c = tokenManager.getClaimsFromToken(systemToken);
            Integer userId = (Integer) c.get("userId");

            googleOAuthService.processGoogleCallback(userId, code, state);

            return ResponseEntity.ok("Google account connected and email saved successfully!");

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (OAuthException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
