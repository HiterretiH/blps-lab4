package org.lab1.service;

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

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenManager tokenManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenManager = tokenManager;
    }

    public Token authenticateUser(String username, String password) {
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

        return token;
    }

    public User registerUser(String username, String email, String password, Role role) {
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
}
