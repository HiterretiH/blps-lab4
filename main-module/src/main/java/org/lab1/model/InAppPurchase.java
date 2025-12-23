package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class InAppPurchase {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne private MonetizedApplication monetizedApplication;

  private String title;
  private String description;
  private double price;

  public final int getId() {
    return id;
  }

  public final void setId(final int idParam) {
    this.id = idParam;
  }

  public final MonetizedApplication getMonetizedApplication() {
    return monetizedApplication;
  }

  public final void setMonetizedApplication(final MonetizedApplication monetizedApplicationParam) {
    this.monetizedApplication = monetizedApplicationParam;
  }

  public final String getTitle() {
    return title;
  }

  public final void setTitle(final String titleParam) {
    this.title = titleParam;
  }

  public final String getDescription() {
    return description;
  }

  public final void setDescription(final String descriptionParam) {
    this.description = descriptionParam;
  }

  public final double getPrice() {
    return price;
  }

  public final void setPrice(final double priceParam) {
    this.price = priceParam;
  }
}
