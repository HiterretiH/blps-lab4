package org.lab1.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Optional;
import org.lab1.exception.NotFoundException;
import org.lab1.exception.UnauthorizedException;
import org.lab1.json.Token;
import org.lab1.model.User;
import org.lab1.repository.UserRepository;
import org.lab1.security.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserQueryService {
  private static final String AUTH_LOGIN_METRIC = "auth.login";
  private static final String STATUS_TAG = "status";
  private static final String SUCCESS_TAG = "success";
  private static final String FAIL_TAG = "fail";
  private static final String INVALID_CREDENTIALS_MSG = "Invalid username or password";
  private static final String USER_NOT_FOUND_MSG = "User not found";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenManager tokenManager;
  private final Counter loginSuccessCounter;
  private final Counter loginFailCounter;

  @Autowired
  public UserQueryService(
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
  }

  public final Token authenticateUser(final String username, final String password) {
    try {
      Optional<User> userOptional = userRepository.findByUsername(username);
      if (userOptional.isEmpty()) {
        throw new UnauthorizedException(INVALID_CREDENTIALS_MSG);
      }

      User user = userOptional.get();

      if (!passwordEncoder.matches(password, user.getPasswordHash())) {
        throw new UnauthorizedException(INVALID_CREDENTIALS_MSG);
      }

      String tokenString =
          tokenManager.generateToken(user.getUsername(), user.getRole().toString(), user.getId());

      Token token = new Token();
      token.setToken(tokenString);
      token.setExpirationDate(
          tokenManager.getClaimsFromToken(tokenString).getExpiration().getTime());
      token.setRole(user.getRole());
      loginSuccessCounter.increment();
      return token;
    } catch (UnauthorizedException unauthorizedException) {
      loginFailCounter.increment();
      throw unauthorizedException;
    }
  }

  public final User getUserById(final int userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MSG));
  }

  public final User getCurrentAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getPrincipal().toString();

    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new UnauthorizedException(USER_NOT_FOUND_MSG));
  }

  public final User getUserByUsername(final String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MSG));
  }

  public final boolean userExists(final int userId) {
    return userRepository.existsById(userId);
  }
}
