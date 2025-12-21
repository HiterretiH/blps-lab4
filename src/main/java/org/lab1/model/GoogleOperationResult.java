package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class GoogleOperationResult {
    @Id
    private Long id;
    private Integer userId;
    private String operation;
    private String targetValue;
    private String result;
    private String error;
    private LocalDateTime createdAt;

    public GoogleOperationResult() {}

    public GoogleOperationResult(Long id, Integer userId, String operation,
                                 String targetValue, String result,
                                 String error, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.operation = operation;
        this.targetValue = targetValue;
        this.result = result;
        this.error = error;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
