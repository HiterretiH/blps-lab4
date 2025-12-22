package org.lab1.model;

import jakarta.persistence.*;

@Entity
public class FormField {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String fieldName;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }
}
