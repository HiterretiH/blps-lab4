package org.lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(unique = true, nullable = false)
  private String username;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Column(unique = true, nullable = false)
  private String email;

  private String passwordHash;

  public final int getId() {
    return id;
  }

  public final void setId(final int idParam) {
    this.id = idParam;
  }

  public final String getUsername() {
    return username;
  }

  public final void setUsername(final String usernameParam) {
    this.username = usernameParam;
  }

  public final String getEmail() {
    return email;
  }

  public final void setEmail(final String emailParam) {
    this.email = emailParam;
  }

  public final Role getRole() {
    return role;
  }

  public final void setRole(final Role roleParam) {
    this.role = roleParam;
  }

  public final String getPasswordHash() {
    return passwordHash;
  }

  public final void setPasswordHash(final String passwordHashParam) {
    this.passwordHash = passwordHashParam;
  }
}
