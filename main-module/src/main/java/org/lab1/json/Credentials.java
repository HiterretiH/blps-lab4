package org.lab1.json;

public final class Credentials {
  private String username;
  private String email;
  private String password;

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

  public String getPassword() {
    return password;
  }

  public void setPassword(final String passwordParam) {
    this.password = passwordParam;
  }
}
