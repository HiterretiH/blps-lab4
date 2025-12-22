package org.lab1.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PaymentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int applicationId;
    private double amount;
    private LocalDateTime requestTime;
    private boolean isCardValid;

    public PaymentRequest(int applicationId, double amount) {
        this.applicationId = applicationId;
        this.amount = amount;
        this.requestTime = LocalDateTime.now();
        this.isCardValid = Math.random() < 0.7;
    }

    public PaymentRequest() {
        this.requestTime = LocalDateTime.now();
        this.isCardValid = Math.random() < 0.7;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public boolean isCardValid() {
        return isCardValid;
    }

    public void setCardValid(boolean cardValid) {
        isCardValid = cardValid;
    }
}
