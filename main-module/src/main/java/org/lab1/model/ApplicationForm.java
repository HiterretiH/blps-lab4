package org.lab1.model;

import lombok.Data;
import java.util.Map;

@Data
public class ApplicationForm {
  private Map<String, String> formFields;
}
