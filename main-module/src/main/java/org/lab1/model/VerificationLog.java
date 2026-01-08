package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class VerificationLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private boolean securityCheckPassed;
  private boolean policyCheckPassed;
  private boolean adsCheckPassed;

  @ManyToOne private Application application;

  private String logMessage;
}
