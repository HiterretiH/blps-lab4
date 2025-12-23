package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class PaymentRequest {
  private static final double CARD_VALID_PROBABILITY = 0.7;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private int applicationId;
  private double amount;
  private LocalDateTime requestTime;
  private boolean isCardValid;

  public PaymentRequest(final int applicationId, final double amount) {
    this.applicationId = applicationId;
    this.amount = amount;
    this.requestTime = LocalDateTime.now();
    this.isCardValid = Math.random() < CARD_VALID_PROBABILITY;
  }

  public PaymentRequest() {
    this.requestTime = LocalDateTime.now();
    this.isCardValid = Math.random() < CARD_VALID_PROBABILITY;
  }

  public final int getId() {
    return id;
  }

  public final void setId(final int id) {
    this.id = id;
  }

  public final int getApplicationId() {
    return applicationId;
  }

  public final void setApplicationId(final int applicationId) {
    this.applicationId = applicationId;
  }

  public final double getAmount() {
    return amount;
  }

  public final void setAmount(final double amount) {
    this.amount = amount;
  }

  public final LocalDateTime getRequestTime() {
    return requestTime;
  }

  public final void setRequestTime(final LocalDateTime requestTime) {
    this.requestTime = requestTime;
  }

  public final boolean isCardValid() {
    return isCardValid;
  }

  public final void setCardValid(final boolean cardValid) {
    this.isCardValid = cardValid;
  }
}
