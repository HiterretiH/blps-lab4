package org.lab1.model;

import jakarta.persistence.*;

@Entity
public class ApplicationStats {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @OneToOne private Application application;

  private long downloads;
  private float rating;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  public long getDownloads() {
    return downloads;
  }

  public void setDownloads(long downloads) {
    this.downloads = downloads;
  }

  public float getRating() {
    return rating;
  }

  public void setRating(float rating) {
    this.rating = rating;
  }
}
