package org.lab1.model;

import jakarta.persistence.*;

@Entity
public class VerificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private boolean securityCheckPassed;
    private boolean policyCheckPassed;
    private boolean adsCheckPassed;

    @ManyToOne
    private Application application;

    private String logMessage;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSecurityCheckPassed() {
        return securityCheckPassed;
    }

    public void setSecurityCheckPassed(boolean securityCheckPassed) {
        this.securityCheckPassed = securityCheckPassed;
    }

    public boolean isPolicyCheckPassed() {
        return policyCheckPassed;
    }

    public void setPolicyCheckPassed(boolean policyCheckPassed) {
        this.policyCheckPassed = policyCheckPassed;
    }

    public boolean isAdsCheckPassed() {
        return adsCheckPassed;
    }

    public void setAdsCheckPassed(boolean adsCheckPassed) {
        this.adsCheckPassed = adsCheckPassed;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }
}
