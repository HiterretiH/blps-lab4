package org.lab1.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @Override
  public void commence(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AuthenticationException authException)
      throws IOException {

    // Обрабатываем только случаи, когда нет заголовка Authorization
    // Для случаев с невалидным токеном уже обработано в JwtAuthorizationFilter
    if (request.getHeader("Authorization") == null) {
      String timestamp = LocalDateTime.now().format(DATE_FORMATTER);

      String jsonResponse =
          String.format(
              "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"errorCode\":\"%s\",\"message\":\"%s\"}",
              timestamp,
              HttpStatus.UNAUTHORIZED.value(),
              HttpStatus.UNAUTHORIZED.getReasonPhrase(),
              "AUTHENTICATION_REQUIRED",
              "Authentication is required to access this resource");

      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");

      try (PrintWriter writer = response.getWriter()) {
        writer.write(jsonResponse);
        writer.flush();
      }
    }
  }
}
