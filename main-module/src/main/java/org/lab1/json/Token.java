package org.lab1.json;

import lombok.Data;
import org.lab1.model.Role;

@Data
public final class Token {
  private String token;
  private long expirationDate;
  private Role role;
}
