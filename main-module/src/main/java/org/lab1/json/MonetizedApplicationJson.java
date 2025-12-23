package org.lab1.json;

public final class MonetizedApplicationJson {
  private int developerId;
  private int applicationId;
  private double currentBalance;
  private double revenue;
  private double downloadRevenue;
  private double adsRevenue;
  private double purchasesRevenue;

  public int getDeveloperId() {
    return developerId;
  }

  public void setDeveloperId(final int developerIdParam) {
    this.developerId = developerIdParam;
  }

  public int getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(final int applicationIdParam) {
    this.applicationId = applicationIdParam;
  }

  public double getCurrentBalance() {
    return currentBalance;
  }

  public void setCurrentBalance(final double currentBalanceParam) {
    this.currentBalance = currentBalanceParam;
  }

  public double getRevenue() {
    return revenue;
  }

  public void setRevenue(final double revenueParam) {
    this.revenue = revenueParam;
  }

  public double getDownloadRevenue() {
    return downloadRevenue;
  }

  public void setDownloadRevenue(final double downloadRevenueParam) {
    this.downloadRevenue = downloadRevenueParam;
  }

  public double getAdsRevenue() {
    return adsRevenue;
  }

  public void setAdsRevenue(final double adsRevenueParam) {
    this.adsRevenue = adsRevenueParam;
  }

  public double getPurchasesRevenue() {
    return purchasesRevenue;
  }

  public void setPurchasesRevenue(final double purchasesRevenueParam) {
    this.purchasesRevenue = purchasesRevenueParam;
  }
}
