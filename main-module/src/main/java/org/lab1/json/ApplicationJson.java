package org.lab1.json;

import org.lab1.model.ApplicationStatus;
import org.lab1.model.ApplicationType;

public class ApplicationJson {
  private int developerId;
  private String name;
  private ApplicationType type;
  private double price;
  private String description;
  private ApplicationStatus status;

  public int getDeveloperId() {
    return developerId;
  }

  public void setDeveloperId(int developerId) {
    this.developerId = developerId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ApplicationType getType() {
    return type;
  }

  public void setType(ApplicationType type) {
    this.type = type;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ApplicationStatus getStatus() {
    return status;
  }

  public void setStatus(ApplicationStatus status) {
    this.status = status;
  }
}
