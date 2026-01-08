package org.lab1.json;

import lombok.Data;

@Data
public final class InAppAddJson {
  private int monetizedApplicationId;
  private String title;
  private String description;
  private double price;
}
