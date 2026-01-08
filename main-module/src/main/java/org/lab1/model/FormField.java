package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class FormField {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String fieldName;
}
