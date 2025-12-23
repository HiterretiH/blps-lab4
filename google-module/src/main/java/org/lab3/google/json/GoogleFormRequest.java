package org.lab3.google.json;

import java.util.Map;

public final class GoogleFormRequest {
  private String googleEmail;
  private Map<String, String> fields;
  private String formTitle;

  public GoogleFormRequest() {
  }

  public GoogleFormRequest(final String googleEmailParam,
                           final Map<String, String> fieldsParam,
                           final String formTitleParam) {
    this.googleEmail = googleEmailParam;
    this.fields = fieldsParam;
    this.formTitle = formTitleParam;
  }

  public String getGoogleEmail() {
    return googleEmail;
  }

  public void setGoogleEmail(final String googleEmailParam) {
    this.googleEmail = googleEmailParam;
  }

  public Map<String, String> getFields() {
    return fields;
  }

  public void setFields(final Map<String, String> fieldsParam) {
    this.fields = fieldsParam;
  }

  public String getFormTitle() {
    return formTitle;
  }

  public void setFormTitle(final String formTitleParam) {
    this.formTitle = formTitleParam;
  }
}
