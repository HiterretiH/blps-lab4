package org.lab3.google.json;

public final class MonetizationEvent {
  public enum EventType {
    DOWNLOAD,
    PURCHASE,
    AD_VIEW
  }

  private EventType eventType;
  private int userId;
  private int applicationId;
  private int itemId;
  private double amount;

  public MonetizationEvent() {}

  public MonetizationEvent(
      final EventType eventTypeParam,
      final int userIdParam,
      final int applicationIdParam,
      final int itemIdParam,
      final double amountParam) {
    this.eventType = eventTypeParam;
    this.userId = userIdParam;
    this.applicationId = applicationIdParam;
    this.itemId = itemIdParam;
    this.amount = amountParam;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(final EventType eventTypeParam) {
    this.eventType = eventTypeParam;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(final int userIdParam) {
    this.userId = userIdParam;
  }

  public int getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(final int applicationIdParam) {
    this.applicationId = applicationIdParam;
  }

  public int getItemId() {
    return itemId;
  }

  public void setItemId(final int itemIdParam) {
    this.itemId = itemIdParam;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(final double amountParam) {
    this.amount = amountParam;
  }
}
