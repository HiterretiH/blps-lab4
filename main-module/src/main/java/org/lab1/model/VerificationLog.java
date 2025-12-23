package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class VerificationLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private boolean securityCheckPassed;
  private boolean policyCheckPassed;
  private boolean adsCheckPassed;

  @ManyToOne private Application application;

  private String logMessage;

  public final int getId() {
    return id;
  }

  public final void setId(final int id) {
    this.id = id;
  }

  public final boolean isSecurityCheckPassed() {
    return securityCheckPassed;
  }

  public final void setSecurityCheckPassed(final boolean securityCheckPassed) {
    this.securityCheckPassed = securityCheckPassed;
  }

  public final boolean isPolicyCheckPassed() {
    return policyCheckPassed;
  }

  public final void setPolicyCheckPassed(final boolean policyCheckPassed) {
    this.policyCheckPassed = policyCheckPassed;
  }

  public final boolean isAdsCheckPassed() {
    return adsCheckPassed;
  }

  public final void setAdsCheckPassed(final boolean adsCheckPassed) {
    this.adsCheckPassed = adsCheckPassed;
  }

  public final Application getApplication() {
    return application;
  }

  public final void setApplication(final Application application) {
    this.application = application;
  }

  public final String getLogMessage() {
    return logMessage;
  }

  public final void setLogMessage(final String logMessage) {
    this.logMessage = logMessage;
  }
}
