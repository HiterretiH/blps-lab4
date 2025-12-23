package org.lab1.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Developer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String name;
  private String description;

  @OneToOne(cascade = CascadeType.ALL)
  private User user;

  public final int getId() {
    return id;
  }

  public final void setId(final int id) {
    this.id = id;
  }

  public final String getName() {
    return name;
  }

  public final void setName(final String name) {
    this.name = name;
  }

  public final String getDescription() {
    return description;
  }

  public final void setDescription(final String description) {
    this.description = description;
  }

  public final User getUser() {
    return user;
  }

  public final void setUser(final User user) {
    this.user = user;
  }
}
