package org.lab1.json;

public final class ApplicationStatsJson {
  private int id;
  private int applicationId;
  private long downloads;
  private float rating;

  public int getId() {
    return id;
  }

  public void setId(final int idParam) {
    this.id = idParam;
  }

  public int getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(final int applicationIdParam) {
    this.applicationId = applicationIdParam;
  }

  public long getDownloads() {
    return downloads;
  }

  public void setDownloads(final long downloadsParam) {
    this.downloads = downloadsParam;
  }

  public float getRating() {
    return rating;
  }

  public void setRating(final float ratingParam) {
    this.rating = ratingParam;
  }
}
