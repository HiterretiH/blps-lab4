package org.lab1.json;

public final class InAppAddJson {
  private int monetizedApplicationId;
  private String title;
  private String description;
  private double price;

  public int getMonetizedApplicationId() {
    return monetizedApplicationId;
  }

  public void setMonetizedApplicationId(final int monetizedApplicationIdParam) {
    this.monetizedApplicationId = monetizedApplicationIdParam;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String titleParam) {
    this.title = titleParam;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String descriptionParam) {
    this.description = descriptionParam;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(final double priceParam) {
    this.price = priceParam;
  }
}
