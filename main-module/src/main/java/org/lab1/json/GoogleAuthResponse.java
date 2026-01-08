package org.lab1.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class GoogleAuthResponse {
  private String authUrl;
  private String state;
  private String message;
}
