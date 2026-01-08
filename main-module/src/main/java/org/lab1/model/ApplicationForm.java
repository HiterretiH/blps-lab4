package org.lab1.model;

import java.util.Map;
import lombok.Data;

@Data
public class ApplicationForm {
  private Map<String, String> formFields;
}
