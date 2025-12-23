package org.lab1.json;

import org.lab1.model.ApplicationStatus;
import org.lab1.model.ApplicationType;

public final class ApplicationJson {
  private int developerId;
  private String name;
  private ApplicationType type;
  private double price;
  private String description;
  private ApplicationStatus status;

  public int getDeveloperId() {
    return developerId;
  }

  public void setDeveloperId(final int developerIdParam) {
    this.developerId = developerIdParam;
  }

  public String getName() {
    return name;
  }

  public void setName(final String nameParam) {
    this.name = nameParam;
  }

  public ApplicationType getType() {
    return type;
  }

  public void setType(final ApplicationType typeParam) {
    this.type = typeParam;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(final double priceParam) {
    this.price = priceParam;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String descriptionParam) {
    this.description = descriptionParam;
  }

  public ApplicationStatus getStatus() {
    return status;
  }

  public void setStatus(final ApplicationStatus statusParam) {
    this.status = statusParam;
  }
}
