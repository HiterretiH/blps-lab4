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

  public final void setId(final int id) {
    this.id = id;
  }

  public final Application getApplication() {
    return application;
  }

  public final void setApplication(final Application application) {
    this.application = application;
  }

  public final long getDownloads() {
    return downloads;
  }

  public final void setDownloads(final long downloads) {
    this.downloads = downloads;
  }

  public final float getRating() {
    return rating;
  }

  public final void setRating(final float rating) {
    this.rating = rating;
  }
}
