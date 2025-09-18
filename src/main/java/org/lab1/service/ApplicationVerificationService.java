package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.model.ApplicationForm;
import org.lab1.model.VerificationLog;
import org.lab1.repository.VerificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
public class ApplicationVerificationService {

    private final VerificationLogRepository verificationLogRepository;
    private final Logger logger;
    private final Random random = new Random();

    @Autowired
    public ApplicationVerificationService(VerificationLogRepository verificationLogRepository, Logger logger) {
        this.verificationLogRepository = verificationLogRepository;
        this.logger = logger;
    }

    public VerificationLog verifyApplicationForm(ApplicationForm form) {
        logger.info("Verifying application form.");
        Map<String, String> formFields = form.getFormFields();

        boolean securityCheckPassed = random.nextInt(100) < 60;
        boolean policyCheckPassed = securityCheckPassed && random.nextInt(100) < 60;
        boolean adsCheckPassed = policyCheckPassed && random.nextInt(100) < 60;

        StringBuilder logMessage = new StringBuilder();
        if (securityCheckPassed) {
            logMessage.append("Security check passed. ");
        } else {
            logMessage.append("Security check failed. ");
            VerificationLog log = saveVerificationLog(form, false, false, false, logMessage.toString());
            logger.info("Verification log saved (security failed) with ID: " + log.getId());
            return log;
        }

        if (policyCheckPassed) {
            logMessage.append("Policy check passed. ");
        } else {
            logMessage.append("Policy check failed. ");
            VerificationLog log = saveVerificationLog(form, true, false, false, logMessage.toString());
            logger.info("Verification log saved (policy failed) with ID: " + log.getId());
            return log;
        }

        if (adsCheckPassed) {
            logMessage.append("Ads check passed.");
        } else {
            logMessage.append("Ads check failed.");
        }

        VerificationLog finalLog = saveVerificationLog(form, true, true, adsCheckPassed, logMessage.toString());
        logger.info("Verification log saved with ID: " + finalLog.getId());
        return finalLog;
    }

    private VerificationLog saveVerificationLog(ApplicationForm form, boolean security, boolean policy, boolean ads, String logMessage) {
        logger.info("Saving verification log.");
        VerificationLog verificationLog = new VerificationLog();
        verificationLog.setSecurityCheckPassed(security);
        verificationLog.setPolicyCheckPassed(policy);
        verificationLog.setAdsCheckPassed(ads);
        verificationLog.setLogMessage(logMessage);
        VerificationLog savedLog = verificationLogRepository.save(verificationLog);
        logger.info("Verification log saved with ID: " + savedLog.getId());
        return savedLog;
    }
}