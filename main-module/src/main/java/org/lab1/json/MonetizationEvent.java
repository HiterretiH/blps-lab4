package org.lab1.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
