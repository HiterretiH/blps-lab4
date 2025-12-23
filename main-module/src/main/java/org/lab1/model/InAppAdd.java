package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class InAppAdd {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  private MonetizedApplication monetizedApplication;

  private String title;
  private String description;
  private double price;

  public final int getId() {
    return id;
  }

  public final void setId(final int id) {
    this.id = id;
  }

  public final MonetizedApplication getMonetizedApplication() {
    return monetizedApplication;
  }

  public final void setMonetizedApplication(final MonetizedApplication monetizedApplication) {
    this.monetizedApplication = monetizedApplication;
  }

  public final String getTitle() {
    return title;
  }

  public final void setTitle(final String title) {
    this.title = title;
  }

  public final String getDescription() {
    return description;
  }

  public final void setDescription(final String description) {
    this.description = description;
  }

  public final double getPrice() {
    return price;
  }

  public final void setPrice(final double price) {
    this.price = price;
  }
}
