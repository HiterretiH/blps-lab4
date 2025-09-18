package org.lab1.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.lab1.model.Role;
import org.lab1.json.Token;
import org.lab1.model.User;
import org.lab1.repository.UserRepository;
import org.lab1.security.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenManager tokenManager;
    private final MeterRegistry meterRegistry;
    private final Counter loginSuccessCounter;
    private final Counter loginFailCounter;
    private final Counter registerSuccessCounter;
    private final Counter registerFailCounter;
    private final Counter tokenGeneratedCounter;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenManager tokenManager, MeterRegistry meterRegistry) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenManager = tokenManager;
        this.meterRegistry = meterRegistry;
        this.loginSuccessCounter = Counter.builder("auth.login")
                .tag("status", "success")
                .register(meterRegistry);

        this.loginFailCounter = Counter.builder("auth.login")
                .tag("status", "fail")
                .register(meterRegistry);

        this.registerSuccessCounter = Counter.builder("auth.register")
                .tag("status", "success")
                .register(meterRegistry);

        this.registerFailCounter = Counter.builder("auth.register")
                .tag("status", "fail")
                .register(meterRegistry);

        this.tokenGeneratedCounter = Counter.builder("auth.tokens.issued")
                .register(meterRegistry);
    }

    public Token authenticateUser(String username, String password) {
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }

            User user = userOptional.get();

            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }

            String tokenString = tokenManager.generateToken(
                    user.getUsername(),
                    user.getRole().toString(),
                    user.getId()
            );

            Token token = new Token();
            token.setToken(tokenString);
            token.setExpirationDate(tokenManager.getClaimsFromToken(tokenString).getExpiration().getTime());
            token.setRole(user.getRole());
            tokenGeneratedCounter.increment();
            loginSuccessCounter.increment();
            return token;
        }
        catch (ResponseStatusException e){
            loginFailCounter.increment();
            throw e;
        }
    }

    public User registerUser(String username, String email, String password, Role role) {
        try {
            if (userRepository.existsByUsername(username)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            }
            if (userRepository.existsByEmail(email)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }

            String passwordHash = passwordEncoder.encode(password);

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setRole(role);
            newUser.setEmail(email);
            newUser.setPasswordHash(passwordHash);

            return userRepository.save(newUser);
        }
        catch (ResponseStatusException e){
            registerFailCounter.increment();
            throw e;
        }
    }

    public Token generateToken(User user) {
        String tokenString = tokenManager.generateToken(
                user.getUsername(),
                user.getRole().toString(),
                user.getId()
        );

        Token token = new Token();
        token.setToken(tokenString);
        token.setExpirationDate(tokenManager.getClaimsFromToken(tokenString).getExpiration().getTime());
        token.setRole(user.getRole());

        return token;
    }

    public Token registerUserAndGetToken(String username, String email, String password, Role role) {
        User savedUser = registerUser(username, email, password, role);
        return generateToken(savedUser);
    }

    public User getUserById(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
