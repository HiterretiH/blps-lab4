package org.lab1.json;

import org.lab1.model.Role;

public final class UserJson {
  private int id;
  private String username;
  private String email;
  private Role role;

  public int getId() {
    return id;
  }

  public void setId(final int idParam) {
    this.id = idParam;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String usernameParam) {
    this.username = usernameParam;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String emailParam) {
    this.email = emailParam;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(final Role roleParam) {
    this.role = roleParam;
  }
}
