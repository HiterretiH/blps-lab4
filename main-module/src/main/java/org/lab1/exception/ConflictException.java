package org.lab1.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {
  private static final HttpStatus STATUS = HttpStatus.CONFLICT;
  private static final String ERROR_CODE = "CONFLICT";

  public ConflictException(final String message) {
    super(message, STATUS, ERROR_CODE);
  }

  public ConflictException(final String message, final Throwable cause) {
    super(message, cause, STATUS, ERROR_CODE);
  }
}
