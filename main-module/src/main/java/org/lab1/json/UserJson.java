package org.lab1.json;

import lombok.Data;
import org.lab1.model.Role;

@Data
public final class UserJson {
  private int id;
  private String username;
  private String email;
  private Role role;
}
