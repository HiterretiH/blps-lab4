package org.lab1.json;

import org.lab1.model.Role;

public final class Token {
  private String token;
  private long expirationDate;
  private Role role;

  public String getToken() {
    return token;
  }

  public void setToken(final String tokenParam) {
    this.token = tokenParam;
  }

  public long getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(final long expirationDateParam) {
    this.expirationDate = expirationDateParam;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(final Role roleParam) {
    this.role = roleParam;
  }
}
