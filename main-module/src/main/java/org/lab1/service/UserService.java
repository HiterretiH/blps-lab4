package org.lab1.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Optional;
import org.lab1.json.Token;
import org.lab1.model.Role;
import org.lab1.model.User;
import org.lab1.repository.UserRepository;
import org.lab1.security.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
  private static final String AUTH_LOGIN_METRIC = "auth.login";
  private static final String STATUS_TAG = "status";
  private static final String SUCCESS_TAG = "success";
  private static final String FAIL_TAG = "fail";
  private static final String AUTH_REGISTER_METRIC = "auth.register";
  private static final String AUTH_TOKENS_METRIC = "auth.tokens.issued";
  private static final String INVALID_CREDENTIALS_MSG = "Invalid username or password";
  private static final String USERNAME_EXISTS_MSG = "Username already exists";
  private static final String EMAIL_EXISTS_MSG = "Email already exists";
  private static final String USER_NOT_FOUND_MSG = "User not found";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenManager tokenManager;
  private final Counter loginSuccessCounter;
  private final Counter loginFailCounter;
  private final Counter registerFailCounter;
  private final Counter tokenGeneratedCounter;

  @Autowired
  public UserService(
      final UserRepository userRepositoryParam,
      final PasswordEncoder passwordEncoderParam,
      final TokenManager tokenManagerParam,
      final MeterRegistry meterRegistry) {
    this.userRepository = userRepositoryParam;
    this.passwordEncoder = passwordEncoderParam;
    this.tokenManager = tokenManagerParam;
    this.loginSuccessCounter =
        Counter.builder(AUTH_LOGIN_METRIC).tag(STATUS_TAG, SUCCESS_TAG).register(meterRegistry);

    this.loginFailCounter =
        Counter.builder(AUTH_LOGIN_METRIC).tag(STATUS_TAG, FAIL_TAG).register(meterRegistry);

    this.registerFailCounter =
        Counter.builder(AUTH_REGISTER_METRIC).tag(STATUS_TAG, FAIL_TAG).register(meterRegistry);

    this.tokenGeneratedCounter = Counter.builder(AUTH_TOKENS_METRIC).register(meterRegistry);
  }

  public final Token authenticateUser(final String username, final String password) {
    try {
      Optional<User> userOptional = userRepository.findByUsername(username);
      if (userOptional.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MSG);
      }

      User user = userOptional.get();

      if (!passwordEncoder.matches(password, user.getPasswordHash())) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MSG);
      }

      String tokenString =
          tokenManager.generateToken(user.getUsername(), user.getRole().toString(), user.getId());

      Token token = new Token();
      token.setToken(tokenString);
      token.setExpirationDate(
          tokenManager.getClaimsFromToken(tokenString).getExpiration().getTime());
      token.setRole(user.getRole());
      tokenGeneratedCounter.increment();
      loginSuccessCounter.increment();
      return token;
    } catch (ResponseStatusException responseStatusException) {
      loginFailCounter.increment();
      throw responseStatusException;
    }
  }

  public final User registerUser(
      final String username, final String email, final String password, final Role role) {
    try {
      if (userRepository.existsByUsername(username)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, USERNAME_EXISTS_MSG);
      }
      if (userRepository.existsByEmail(email)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, EMAIL_EXISTS_MSG);
      }

      String passwordHash = passwordEncoder.encode(password);

      User newUser = new User();
      newUser.setUsername(username);
      newUser.setRole(role);
      newUser.setEmail(email);
      newUser.setPasswordHash(passwordHash);

      return userRepository.save(newUser);
    } catch (ResponseStatusException responseStatusException) {
      registerFailCounter.increment();
      throw responseStatusException;
    }
  }

  public final Token generateToken(final User user) {
    String tokenString =
        tokenManager.generateToken(user.getUsername(), user.getRole().toString(), user.getId());

    Token token = new Token();
    token.setToken(tokenString);
    token.setExpirationDate(tokenManager.getClaimsFromToken(tokenString).getExpiration().getTime());
    token.setRole(user.getRole());

    return token;
  }

  public final Token registerUserAndGetToken(
      final String username, final String email, final String password, final Role role) {
    User savedUser = registerUser(username, email, password, role);
    return generateToken(savedUser);
  }

  public final User getUserById(final int userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MSG));
  }
}
