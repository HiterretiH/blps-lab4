package org.lab3.google.model;

import java.time.LocalDateTime;

public final class GoogleOperationResult {
  private Long id;
  private Integer userId;
  private String operation;
  private String targetValue;
  private String result;
  private String error;
  private LocalDateTime createdAt;

  public GoogleOperationResult() {
  }

  public GoogleOperationResult(
      final Integer userIdParam,
      final String operationParam,
      final String targetValueParam,
      final String resultParam,
      final String errorParam) {
    this.userId = userIdParam;
    this.operation = operationParam;
    this.targetValue = targetValueParam;
    this.result = resultParam;
    this.error = errorParam;
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long idParam) {
    this.id = idParam;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(final Integer userIdParam) {
    this.userId = userIdParam;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(final String operationParam) {
    this.operation = operationParam;
  }

  public String getTargetValue() {
    return targetValue;
  }

  public void setTargetValue(final String targetValueParam) {
    this.targetValue = targetValueParam;
  }

  public String getResult() {
    return result;
  }

  public void setResult(final String resultParam) {
    this.result = resultParam;
  }

  public String getError() {
    return error;
  }

  public void setError(final String errorParam) {
    this.error = errorParam;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final LocalDateTime createdAtParam) {
    this.createdAt = createdAtParam;
  }
}
