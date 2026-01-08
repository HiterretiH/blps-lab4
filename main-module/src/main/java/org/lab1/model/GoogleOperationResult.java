package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleOperationResult {
  @Id
  private Long id;
  private Integer userId;
  private String operation;
  private String targetValue;
  private String result;
  private String error;
  private LocalDateTime createdAt;
}
