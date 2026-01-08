package org.lab1.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {
  private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
  private static final String ERROR_CODE = "UNAUTHORIZED";

  public UnauthorizedException(final String message) {
    super(message, STATUS, ERROR_CODE);
  }

  public UnauthorizedException(final String message, final Throwable cause) {
    super(message, cause, STATUS, ERROR_CODE);
  }
}
