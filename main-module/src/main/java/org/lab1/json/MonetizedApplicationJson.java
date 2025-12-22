package org.lab1.json;

public class MonetizedApplicationJson {

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

    public void setDeveloperId(int developerId) {
        this.developerId = developerId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
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
