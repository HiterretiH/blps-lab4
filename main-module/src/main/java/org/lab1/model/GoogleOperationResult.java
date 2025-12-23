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
      final Long id,
      final Integer userId,
      final String operation,
      final String targetValue,
      final String result,
      final String error,
      final LocalDateTime createdAt) {
    this.id = id;
    this.userId = userId;
    this.operation = operation;
    this.targetValue = targetValue;
    this.result = result;
    this.error = error;
    this.createdAt = createdAt;
  }

  public final Long getId() {
    return id;
  }

  public final void setId(final Long id) {
    this.id = id;
  }

  public final Integer getUserId() {
    return userId;
  }

  public final void setUserId(final Integer userId) {
    this.userId = userId;
  }

  public final String getOperation() {
    return operation;
  }

  public final void setOperation(final String operation) {
    this.operation = operation;
  }

  public final String getTargetValue() {
    return targetValue;
  }

  public final void setTargetValue(final String targetValue) {
    this.targetValue = targetValue;
  }

  public final String getResult() {
    return result;
  }

  public final void setResult(final String result) {
    this.result = result;
  }

  public final String getError() {
    return error;
  }

  public final void setError(final String error) {
    this.error = error;
  }

  public final LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public final void setCreatedAt(final LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
