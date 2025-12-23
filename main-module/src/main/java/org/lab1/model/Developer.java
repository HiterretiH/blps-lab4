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

  public final void setId(final int idParam) {
    this.id = idParam;
  }

  public final String getName() {
    return name;
  }

  public final void setName(final String nameParam) {
    this.name = nameParam;
  }

  public final String getDescription() {
    return description;
  }

  public final void setDescription(final String descriptionParam) {
    this.description = descriptionParam;
  }

  public final User getUser() {
    return user;
  }

  public final void setUser(final User userParam) {
    this.user = userParam;
  }
}
