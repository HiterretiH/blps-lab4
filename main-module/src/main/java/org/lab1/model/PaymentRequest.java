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

  public final int getId() {
    return id;
  }

  public final void setId(final int idParam) {
    this.id = idParam;
  }

  public final int getApplicationId() {
    return applicationId;
  }

  public final void setApplicationId(final int applicationIdParam) {
    this.applicationId = applicationIdParam;
  }

  public final double getAmount() {
    return amount;
  }

  public final void setAmount(final double amountParam) {
    this.amount = amountParam;
  }

  public final LocalDateTime getRequestTime() {
    return requestTime;
  }

  public final void setRequestTime(final LocalDateTime requestTimeParam) {
    this.requestTime = requestTimeParam;
  }

  public final boolean isCardValid() {
    return isCardValid;
  }

  public final void setCardValid(final boolean cardValidParam) {
    this.isCardValid = cardValidParam;
  }
}
