package org.lab1.json;

import lombok.Data;

@Data
public final class MonetizedApplicationJson {
  private int developerId;
  private int applicationId;
  private double currentBalance;
  private double revenue;
  private double downloadRevenue;
  private double adsRevenue;
  private double purchasesRevenue;
}
