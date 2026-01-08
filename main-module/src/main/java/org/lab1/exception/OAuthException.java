package org.lab1.exception;

import org.springframework.http.HttpStatus;

public class OAuthException extends ApiException {
  private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
  private static final String ERROR_CODE = "OAUTH_ERROR";

  public OAuthException(final String message) {
    super(message, STATUS, ERROR_CODE);
  }

  public OAuthException(final String message, final Throwable cause) {
    super(message, cause, STATUS, ERROR_CODE);
  }
}
