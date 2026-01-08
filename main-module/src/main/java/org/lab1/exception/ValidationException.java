package org.lab1.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApiException {
  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String ERROR_CODE = "VALIDATION_ERROR";

  public ValidationException(final String message) {
    super(message, STATUS, ERROR_CODE);
  }

  public ValidationException(final String message, final Throwable cause) {
    super(message, cause, STATUS, ERROR_CODE);
  }
}
