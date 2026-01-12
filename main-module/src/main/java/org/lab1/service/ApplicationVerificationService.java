package org.lab1.service;

import java.util.Map;
import java.util.Random;
import org.lab.logger.Logger;
import org.lab1.model.ApplicationForm;
import org.lab1.model.VerificationLog;
import org.lab1.repository.VerificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
  private static final String VERIFICATION_LOG_SAVED_LOG =
      "Verification log saved (security failed) with ID: ";
  private static final String VERIFICATION_POLICY_SAVED_LOG =
      "Verification log saved (policy failed) with ID: ";
  private static final String VERIFICATION_FINAL_SAVED_LOG = "Verification log saved with ID: ";
  private static final String SAVE_VERIFICATION_LOG = "Saving verification log.";
  private static final String VERIFICATION_SAVED_LOG = "Verification log saved with ID: ";

  private final VerificationLogRepository verificationLogRepository;
  private final Logger logger;
  private final Random random = new Random();

  @Autowired
  public ApplicationVerificationService(
      final VerificationLogRepository verificationLogRepositoryParam,
      @Qualifier("correlationLogger") final Logger loggerParam) {
    this.verificationLogRepository = verificationLogRepositoryParam;
    this.logger = loggerParam;
  }

  public final VerificationLog verifyApplicationForm(final ApplicationForm form) {
    logger.info(VERIFY_FORM_LOG);
    Map<String, String> formFields = form.getFormFields();

    final int hundredPercent = 100;
    boolean securityCheckPassed = random.nextInt(hundredPercent) < SECURITY_CHECK_THRESHOLD;
    boolean policyCheckPassed =
        securityCheckPassed && random.nextInt(hundredPercent) < POLICY_CHECK_THRESHOLD;
    boolean adsCheckPassed =
        policyCheckPassed && random.nextInt(hundredPercent) < ADS_CHECK_THRESHOLD;

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

    VerificationLog finalLog =
        saveVerificationLog(form, true, true, adsCheckPassed, logMessage.toString());
    logger.info(VERIFICATION_FINAL_SAVED_LOG + finalLog.getId());
    return finalLog;
  }

  private VerificationLog saveVerificationLog(
      final ApplicationForm form,
      final boolean security,
      final boolean policy,
      final boolean ads,
      final String logMessage) {
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
