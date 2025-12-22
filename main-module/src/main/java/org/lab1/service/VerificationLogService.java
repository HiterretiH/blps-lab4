package org.lab1.service;

import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.model.VerificationLog;
import org.lab1.repository.VerificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationLogService {
  private static final String CREATE_LOG = "Creating VerificationLog";
  private static final String GET_LOG = "Getting VerificationLog by ID: ";

  private final VerificationLogRepository verificationLogRepository;
  private final Logger logger;

  @Autowired
  public VerificationLogService(
      VerificationLogRepository verificationLogRepository, Logger logger) {
    this.verificationLogRepository = verificationLogRepository;
    this.logger = logger;
  }

  public VerificationLog createVerificationLog(
      boolean securityCheckPassed,
      boolean policyCheckPassed,
      boolean adsCheckPassed,
      String logMessage) {
    logger.info(CREATE_LOG);
    VerificationLog verificationLog = new VerificationLog();
    verificationLog.setSecurityCheckPassed(securityCheckPassed);
    verificationLog.setPolicyCheckPassed(policyCheckPassed);
    verificationLog.setAdsCheckPassed(adsCheckPassed);
    verificationLog.setLogMessage(logMessage);
    return verificationLogRepository.save(verificationLog);
  }

  public Optional<VerificationLog> getVerificationLogById(int id) {
    logger.info(GET_LOG + id);
    return verificationLogRepository.findById(id);
  }
}
