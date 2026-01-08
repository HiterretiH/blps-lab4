package org.lab1.json;

import lombok.Data;

@Data
public final class ApplicationStatsJson {
  private int id;
  private int applicationId;
  private long downloads;
  private float rating;
}
