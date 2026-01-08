package org.lab1.json;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class GoogleFormRequest {
  private String googleEmail;
  private Map<String, String> fields;
  private String formTitle;
}
