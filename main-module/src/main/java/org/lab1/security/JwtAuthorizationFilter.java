package org.lab1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Фильтр авторизации JWT, обрабатывающий токены в заголовках запросов.
 * Проверяет наличие и валидность JWT токена, устанавливает контекст безопасности.
 */
@Component
public final class JwtAuthorizationFilter extends OncePerRequestFilter {
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final int BEARER_PREFIX_LENGTH = 7;
  private static final String ROLE_CLAIM = "role";

  private final TokenManager tokenManager;

  @Autowired
  public JwtAuthorizationFilter(final TokenManager tokenManagerParam) {
    this.tokenManager = tokenManagerParam;
  }

  /**
   * Основной метод фильтрации запросов.
   * Извлекает токен из заголовка, проверяет его валидность и устанавливает аутентификацию.
   *
   * @param request HTTP запрос
   * @param response HTTP ответ
   * @param filterChain цепочка фильтров
   * @throws ServletException если возникает ошибка сервлета
   * @throws IOException если возникает ошибка ввода/вывода
   */
  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      @NotNull final HttpServletResponse response,
      @NotNull final FilterChain filterChain)
      throws ServletException, IOException {

    String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

    if (!isValidAuthorizationHeader(authorizationHeader)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = extractToken(authorizationHeader);
    processTokenAuthentication(token, response, filterChain, request);
  }

  private boolean isValidAuthorizationHeader(final String authorizationHeader) {
    return authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX);
  }

  private String extractToken(final String authorizationHeader) {
    return authorizationHeader.substring(BEARER_PREFIX_LENGTH);
  }

  private void processTokenAuthentication(
      final String token,
      final HttpServletResponse response,
      final FilterChain filterChain,
      final HttpServletRequest request)
      throws IOException, ServletException {
    try {
      if (tokenManager.isTokenValid(token)) {
        Claims claims = tokenManager.getClaimsFromToken(token);
        String username = claims.getSubject();
        String role = claims.get(ROLE_CLAIM, String.class);
        List<GrantedAuthority> authorities = tokenManager.getAuthoritiesByRole(role);

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }
    } catch (JwtException jwtException) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      return;
    }

    filterChain.doFilter(request, response);
  }
}
