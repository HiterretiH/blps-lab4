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

  public final void setId(final int idParam) {
    this.id = idParam;
  }

  public final Developer getDeveloper() {
    return developer;
  }

  public final void setDeveloper(final Developer developerParam) {
    this.developer = developerParam;
  }

  public final String getName() {
    return name;
  }

  public final void setName(final String nameParam) {
    this.name = nameParam;
  }

  public final ApplicationType getType() {
    return type;
  }

  public final void setType(final ApplicationType typeParam) {
    this.type = typeParam;
  }

  public final double getPrice() {
    return price;
  }

  public final void setPrice(final double priceParam) {
    this.price = priceParam;
  }

  public final String getDescription() {
    return description;
  }

  public final void setDescription(final String descriptionParam) {
    this.description = descriptionParam;
  }

  public final ApplicationStatus getStatus() {
    return status;
  }

  public final void setStatus(final ApplicationStatus statusParam) {
    this.status = statusParam;
  }
}
