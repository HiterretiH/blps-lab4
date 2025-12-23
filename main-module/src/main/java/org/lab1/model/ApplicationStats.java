package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class ApplicationStats {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @OneToOne private Application application;

  private long downloads;
  private float rating;

  public final int getId() {
    return id;
  }

  public final void setId(final int idParam) {
    this.id = idParam;
  }

  public final Application getApplication() {
    return application;
  }

  public final void setApplication(final Application applicationParam) {
    this.application = applicationParam;
  }

  public final long getDownloads() {
    return downloads;
  }

  public final void setDownloads(final long downloadsParam) {
    this.downloads = downloadsParam;
  }

  public final float getRating() {
    return rating;
  }

  public final void setRating(final float ratingParam) {
    this.rating = ratingParam;
  }
}
