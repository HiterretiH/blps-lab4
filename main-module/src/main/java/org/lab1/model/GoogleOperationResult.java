package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class GoogleOperationResult {
  @Id private Long id;
  private Integer userId;
  private String operation;
  private String targetValue;
  private String result;
  private String error;
  private LocalDateTime createdAt;

  public GoogleOperationResult() {}

  public GoogleOperationResult(
      final Long idConstructor,
      final Integer userIdConstructor,
      final String operationConstructor,
      final String targetValueConstructor,
      final String resultConstructor,
      final String errorConstructor,
      final LocalDateTime createdAtConstructor) {
    this.id = idConstructor;
    this.userId = userIdConstructor;
    this.operation = operationConstructor;
    this.targetValue = targetValueConstructor;
    this.result = resultConstructor;
    this.error = errorConstructor;
    this.createdAt = createdAtConstructor;
  }

  public final Long getId() {
    return id;
  }

  public final void setId(final Long idParam) {
    this.id = idParam;
  }

  public final Integer getUserId() {
    return userId;
  }

  public final void setUserId(final Integer userIdParam) {
    this.userId = userIdParam;
  }

  public final String getOperation() {
    return operation;
  }

  public final void setOperation(final String operationParam) {
    this.operation = operationParam;
  }

  public final String getTargetValue() {
    return targetValue;
  }

  public final void setTargetValue(final String targetValueParam) {
    this.targetValue = targetValueParam;
  }

  public final String getResult() {
    return result;
  }

  public final void setResult(final String resultParam) {
    this.result = resultParam;
  }

  public final String getError() {
    return error;
  }

  public final void setError(final String errorParam) {
    this.error = errorParam;
  }

  public final LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public final void setCreatedAt(final LocalDateTime createdAtParam) {
    this.createdAt = createdAtParam;
  }
}
