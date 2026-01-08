package org.lab1.json;

import lombok.Data;

@Data
public final class InAppPurchaseJson {
  private int id;
  private Integer monetizedApplicationId;
  private String title;
  private String description;
  private double price;
}
