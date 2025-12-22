package org.lab1.json;

import java.util.Map;

public class GoogleFormRequest {
  private String googleEmail;
  private Map<String, String> fields;
  private String formTitle;

  public GoogleFormRequest() {}

  public GoogleFormRequest(String googleEmail, Map<String, String> fields, String formTitle) {
    this.googleEmail = googleEmail;
    this.fields = fields;
    this.formTitle = formTitle;
  }

  public String getGoogleEmail() {
    return googleEmail;
  }

  public void setGoogleEmail(String googleEmail) {
    this.googleEmail = googleEmail;
  }

  public Map<String, String> getFields() {
    return fields;
  }

  public void setFields(Map<String, String> fields) {
    this.fields = fields;
  }

  public String getFormTitle() {
    return formTitle;
  }

  public void setFormTitle(String formTitle) {
    this.formTitle = formTitle;
  }
}
