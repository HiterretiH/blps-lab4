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
    private static final int SECURITY_CHECK_THRESHOLD = 60;
    private static final int POLICY_CHECK_THRESHOLD = 60;
    private static final int ADS_CHECK_THRESHOLD = 60;
    private static final String VERIFY_FORM_LOG = "Verifying application form.";
    private static final String SECURITY_CHECK_PASSED_MSG = "Security check passed. ";
    private static final String SECURITY_CHECK_FAILED_MSG = "Security check failed. ";
    private static final String POLICY_CHECK_PASSED_MSG = "Policy check passed. ";
    private static final String POLICY_CHECK_FAILED_MSG = "Policy check failed. ";
    private static final String ADS_CHECK_PASSED_MSG = "Ads check passed.";
    private static final String ADS_CHECK_FAILED_MSG = "Ads check failed.";
    private static final String VERIFICATION_LOG_SAVED_LOG = "Verification log saved (security failed) with ID: ";
    private static final String VERIFICATION_POLICY_SAVED_LOG = "Verification log saved (policy failed) with ID: ";
    private static final String VERIFICATION_FINAL_SAVED_LOG = "Verification log saved with ID: ";
    private static final String SAVE_VERIFICATION_LOG = "Saving verification log.";
    private static final String VERIFICATION_SAVED_LOG = "Verification log saved with ID: ";

    private final VerificationLogRepository verificationLogRepository;
    private final Logger logger;
    private final Random random = new Random();

    @Autowired
    public ApplicationVerificationService(VerificationLogRepository verificationLogRepository, Logger logger) {
        this.verificationLogRepository = verificationLogRepository;
        this.logger = logger;
    }

    public VerificationLog verifyApplicationForm(ApplicationForm form) {
        logger.info(VERIFY_FORM_LOG);
        Map<String, String> formFields = form.getFormFields();

        boolean securityCheckPassed = random.nextInt(100) < SECURITY_CHECK_THRESHOLD;
        boolean policyCheckPassed = securityCheckPassed && random.nextInt(100) < POLICY_CHECK_THRESHOLD;
        boolean adsCheckPassed = policyCheckPassed && random.nextInt(100) < ADS_CHECK_THRESHOLD;

        StringBuilder logMessage = new StringBuilder();

        if (securityCheckPassed) {
            logMessage.append(SECURITY_CHECK_PASSED_MSG);
        } else {
            logMessage.append(SECURITY_CHECK_FAILED_MSG);
            VerificationLog log = saveVerificationLog(form, false, false, false, logMessage.toString());
            logger.info(VERIFICATION_LOG_SAVED_LOG + log.getId());
            return log;
        }

        if (policyCheckPassed) {
            logMessage.append(POLICY_CHECK_PASSED_MSG);
        } else {
            logMessage.append(POLICY_CHECK_FAILED_MSG);
            VerificationLog log = saveVerificationLog(form, true, false, false, logMessage.toString());
            logger.info(VERIFICATION_POLICY_SAVED_LOG + log.getId());
            return log;
        }

        if (adsCheckPassed) {
            logMessage.append(ADS_CHECK_PASSED_MSG);
        } else {
            logMessage.append(ADS_CHECK_FAILED_MSG);
        }

        VerificationLog finalLog = saveVerificationLog(form, true, true, adsCheckPassed, logMessage.toString());
        logger.info(VERIFICATION_FINAL_SAVED_LOG + finalLog.getId());
        return finalLog;
    }

    private VerificationLog saveVerificationLog(ApplicationForm form, boolean security, boolean policy, boolean ads, String logMessage) {
        logger.info(SAVE_VERIFICATION_LOG);
        VerificationLog verificationLog = new VerificationLog();
        verificationLog.setSecurityCheckPassed(security);
        verificationLog.setPolicyCheckPassed(policy);
        verificationLog.setAdsCheckPassed(ads);
        verificationLog.setLogMessage(logMessage);
        VerificationLog savedLog = verificationLogRepository.save(verificationLog);
        logger.info(VERIFICATION_SAVED_LOG + savedLog.getId());
        return savedLog;
    }
}
