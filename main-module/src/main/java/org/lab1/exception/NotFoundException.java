package org.lab1.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
  private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
  private static final String ERROR_CODE = "NOT_FOUND";

  public NotFoundException(final String message) {
    super(message, STATUS, ERROR_CODE);
  }

  public NotFoundException(final String message, final Throwable cause) {
    super(message, cause, STATUS, ERROR_CODE);
  }
}
