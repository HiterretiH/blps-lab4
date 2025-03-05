package org.lab1.service;

import org.lab1.model.Application;
import org.lab1.model.ApplicationForm;
import org.lab1.model.VerificationLog;
import org.lab1.repository.VerificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
public class ApplicationVerificationService {

    @Autowired
    private VerificationLogRepository verificationLogRepository;

    private Random random = new Random();
    public VerificationLog verifyApplicationForm(ApplicationForm form) {
        Map<String, String> formFields = form.getFormFields();

        boolean securityCheckPassed = random.nextInt(100) < 60;
        boolean policyCheckPassed = securityCheckPassed && random.nextInt(100) < 60;
        boolean adsCheckPassed = policyCheckPassed && random.nextInt(100) < 60;

        StringBuilder logMessage = new StringBuilder();
        if (securityCheckPassed) {
            logMessage.append("Security check passed. ");
        } else {
            logMessage.append("Security check failed. ");
            return saveVerificationLog(form, false, false, false, logMessage.toString());
        }

        if (policyCheckPassed) {
            logMessage.append("Policy check passed. ");
        } else {
            logMessage.append("Policy check failed. ");
            return saveVerificationLog(form, true, false, false, logMessage.toString());
        }

        if (adsCheckPassed) {
            logMessage.append("Ads check passed.");
        } else {
            logMessage.append("Ads check failed.");
        }

        return saveVerificationLog(form, true, true, adsCheckPassed, logMessage.toString());
    }

    private VerificationLog saveVerificationLog(ApplicationForm form, boolean security, boolean policy, boolean ads, String logMessage) {
        VerificationLog verificationLog = new VerificationLog();
        verificationLog.setSecurityCheckPassed(security);
        verificationLog.setPolicyCheckPassed(policy);
        verificationLog.setAdsCheckPassed(ads);
        verificationLog.setLogMessage(logMessage);
        return verificationLogRepository.save(verificationLog);
    }
}
