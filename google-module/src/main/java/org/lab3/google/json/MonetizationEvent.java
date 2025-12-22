package org.lab3.google.json;

public class MonetizationEvent {
  public enum EventType {
    DOWNLOAD,
    PURCHASE,
    AD_VIEW
  }

  public MonetizationEvent() {}

  public MonetizationEvent(
      EventType eventType, int userId, int applicationId, int itemId, double amount) {
    this.eventType = eventType;
    this.userId = userId;
    this.applicationId = applicationId;
    this.itemId = itemId;
    this.amount = amount;
  }

  private EventType eventType;
  private int userId;
  private int applicationId;
  private int itemId; // для purchaseId или adId
  private double amount;

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(int applicationId) {
    this.applicationId = applicationId;
  }

  public int getItemId() {
    return itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }
}
