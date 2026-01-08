package org.lab1.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class GoogleFormRequest {
  private String googleEmail;
  private Map<String, String> fields;
  private String formTitle;
}
