package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

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

  public final Application getApplication() {
    return application;
  }

  public final void setApplication(final Application application) {
    this.application = application;
  }

  public final double getCurrentBalance() {
    return currentBalance;
  }

  public final void setCurrentBalance(final double currentBalance) {
    this.currentBalance = currentBalance;
  }

  public final double getRevenue() {
    return revenue;
  }

  public final void setRevenue(final double revenue) {
    this.revenue = revenue;
  }

  public final double getDownloadRevenue() {
    return downloadRevenue;
  }

  public final void setDownloadRevenue(final double downloadRevenue) {
    this.downloadRevenue = downloadRevenue;
  }

  public final double getAdsRevenue() {
    return adsRevenue;
  }

  public final void setAdsRevenue(final double adsRevenue) {
    this.adsRevenue = adsRevenue;
  }

  public final double getPurchasesRevenue() {
    return purchasesRevenue;
  }

  public final void setPurchasesRevenue(final double purchasesRevenue) {
    this.purchasesRevenue = purchasesRevenue;
  }
}
