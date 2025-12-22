package org.lab1.model;

import jakarta.persistence.*;

@Entity
public class MonetizedApplication {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne private Developer developer;

  @OneToOne private Application application;

  private double currentBalance;
  private double revenue;
  private double downloadRevenue;
  private double adsRevenue;
  private double purchasesRevenue;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Developer getDeveloper() {
    return developer;
  }

  public void setDeveloper(Developer developer) {
    this.developer = developer;
  }

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  public double getCurrentBalance() {
    return currentBalance;
  }

  public void setCurrentBalance(double currentBalance) {
    this.currentBalance = currentBalance;
  }

  public double getRevenue() {
    return revenue;
  }

  public void setRevenue(double revenue) {
    this.revenue = revenue;
  }

  public double getDownloadRevenue() {
    return downloadRevenue;
  }

  public void setDownloadRevenue(double downloadRevenue) {
    this.downloadRevenue = downloadRevenue;
  }

  public double getAdsRevenue() {
    return adsRevenue;
  }

  public void setAdsRevenue(double adsRevenue) {
    this.adsRevenue = adsRevenue;
  }

  public double getPurchasesRevenue() {
    return purchasesRevenue;
  }

  public void setPurchasesRevenue(double purchasesRevenue) {
    this.purchasesRevenue = purchasesRevenue;
  }
}
