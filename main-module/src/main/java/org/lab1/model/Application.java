package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Application {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne private Developer developer;

  private String name;

  @Enumerated(EnumType.STRING)
  private ApplicationType type;

  private double price;
  private String description;
  private ApplicationStatus status;

  public final int getId() {
    return id;
  }

  public final void setId(final int id) {
    this.id = id;
  }

  public final Developer getDeveloper() {
    return developer;
  }

  public final void setDeveloper(final Developer developer) {
    this.developer = developer;
  }

  public final String getName() {
    return name;
  }

  public final void setName(final String name) {
    this.name = name;
  }

  public final ApplicationType getType() {
    return type;
  }

  public final void setType(final ApplicationType type) {
    this.type = type;
  }

  public final double getPrice() {
    return price;
  }

  public final void setPrice(final double price) {
    this.price = price;
  }

  public final String getDescription() {
    return description;
  }

  public final void setDescription(final String description) {
    this.description = description;
  }

  public final ApplicationStatus getStatus() {
    return status;
  }

  public final void setStatus(final ApplicationStatus status) {
    this.status = status;
  }
}
