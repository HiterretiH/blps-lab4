package org.lab1.json;

public final class InAppPurchaseJson {
  private int id;
  private Integer monetizedApplicationId;
  private String title;
  private String description;
  private double price;

  public int getId() {
    return id;
  }

  public void setId(final int idParam) {
    this.id = idParam;
  }

  public Integer getMonetizedApplicationId() {
    return monetizedApplicationId;
  }

  public void setMonetizedApplicationId(final Integer monetizedApplicationIdParam) {
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
