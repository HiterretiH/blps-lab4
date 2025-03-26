package org.lab1.controller;

import jakarta.transaction.Transactional;
import org.lab1.json.Credentials;
import org.lab1.json.LoginCredentials;
import org.lab1.json.Token;
import org.lab1.model.Role;
import org.lab1.model.User;
import org.lab1.service.DeveloperService;
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

@RestController
@RequestMapping("/api/auth")
public class AuthorizationController {
    private final UserService userService;
    private final DeveloperService developerService;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    public AuthorizationController(UserService userService, DeveloperService developerService, PlatformTransactionManager transactionManager) {
        this.userService = userService;
        this.developerService = developerService;
        this.transactionManager = transactionManager;
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
}
