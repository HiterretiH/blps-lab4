package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
public class PaymentRequest {
  private static final double CARD_VALID_PROBABILITY = 0.7;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private int applicationId;
  private double amount;
  private LocalDateTime requestTime;
  private boolean isCardValid;

  public PaymentRequest(final int applicationIdParam, final double amountParam) {
    this.applicationId = applicationIdParam;
    this.amount = amountParam;
    this.requestTime = LocalDateTime.now();
    this.isCardValid = Math.random() < CARD_VALID_PROBABILITY;
  }

  public PaymentRequest() {
    this.requestTime = LocalDateTime.now();
    this.isCardValid = Math.random() < CARD_VALID_PROBABILITY;
  }
}
