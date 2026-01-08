package org.lab1.json;

import lombok.Data;
import org.lab1.model.ApplicationStatus;
import org.lab1.model.ApplicationType;

@Data
public final class ApplicationJson {
  private int developerId;
  private String name;
  private ApplicationType type;
  private double price;
  private String description;
  private ApplicationStatus status;
}
