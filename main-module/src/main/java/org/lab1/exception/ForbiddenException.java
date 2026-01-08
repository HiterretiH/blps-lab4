package org.lab1.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
  private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;
  private static final String ERROR_CODE = "FORBIDDEN";

  public ForbiddenException(final String message) {
    super(message, STATUS, ERROR_CODE);
  }

  public ForbiddenException(final String message, final Throwable cause) {
    super(message, cause, STATUS, ERROR_CODE);
  }
}
