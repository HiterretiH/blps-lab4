package org.lab1.json;

import lombok.Data;

@Data
public final class GoogleAuthRequest {
  private String code;
  private String state;
}
