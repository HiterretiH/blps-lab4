package org.lab1.model;

import java.util.Map;

public class ApplicationForm {
  private Map<String, String> formFields;

  public final Map<String, String> getFormFields() {
    return formFields;
  }

  public final void setFormFields(final Map<String, String> formFields) {
    this.formFields = formFields;
  }
}
