package org.lab1.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {
  private final HttpStatus httpStatus;
  private final String errorCode;

  protected ApiException(
      final String message, final HttpStatus httpStatusParam, final String errorCodeParam) {
    super(message);
    this.httpStatus = httpStatusParam;
    this.errorCode = errorCodeParam;
  }

  protected ApiException(
      final String message,
      final Throwable cause,
      final HttpStatus httpStatusParam,
      final String errorCodeParam) {
    super(message, cause);
    this.httpStatus = httpStatusParam;
    this.errorCode = errorCodeParam;
  }
}
