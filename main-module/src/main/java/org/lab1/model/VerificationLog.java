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

  public final void setId(final int idParam) {
    this.id = idParam;
  }

  public final boolean isSecurityCheckPassed() {
    return securityCheckPassed;
  }

  public final void setSecurityCheckPassed(final boolean securityCheckPassedParam) {
    this.securityCheckPassed = securityCheckPassedParam;
  }

  public final boolean isPolicyCheckPassed() {
    return policyCheckPassed;
  }

  public final void setPolicyCheckPassed(final boolean policyCheckPassedParam) {
    this.policyCheckPassed = policyCheckPassedParam;
  }

  public final boolean isAdsCheckPassed() {
    return adsCheckPassed;
  }

  public final void setAdsCheckPassed(final boolean adsCheckPassedParam) {
    this.adsCheckPassed = adsCheckPassedParam;
  }

  public final Application getApplication() {
    return application;
  }

  public final void setApplication(final Application applicationParam) {
    this.application = applicationParam;
  }

  public final String getLogMessage() {
    return logMessage;
  }

  public final void setLogMessage(final String logMessageParam) {
    this.logMessage = logMessageParam;
  }
}
