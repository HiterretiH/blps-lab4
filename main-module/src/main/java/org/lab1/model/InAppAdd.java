package org.lab1.model;

import jakarta.persistence.*;

@Entity
public class InAppAdd {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne private MonetizedApplication monetizedApplication;

  private String title;
  private String description;
  private double price;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public MonetizedApplication getMonetizedApplication() {
    return monetizedApplication;
  }

  public void setMonetizedApplication(MonetizedApplication monetizedApplication) {
    this.monetizedApplication = monetizedApplication;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }
}
