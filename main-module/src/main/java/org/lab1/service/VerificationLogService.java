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
      final VerificationLogRepository verificationLogRepositoryConstructor, final Logger loggerConstructor) {
    this.verificationLogRepository = verificationLogRepositoryConstructor;
    this.logger = loggerConstructor;
  }

  public final VerificationLog createVerificationLog(
      final boolean securityCheckPassed,
      final boolean policyCheckPassed,
      final boolean adsCheckPassed,
      final String logMessage) {
    logger.info(CREATE_LOG);
    VerificationLog verificationLog = new VerificationLog();
    verificationLog.setSecurityCheckPassed(securityCheckPassed);
    verificationLog.setPolicyCheckPassed(policyCheckPassed);
    verificationLog.setAdsCheckPassed(adsCheckPassed);
    verificationLog.setLogMessage(logMessage);
    return verificationLogRepository.save(verificationLog);
  }

  public final Optional<VerificationLog> getVerificationLogById(final int id) {
    logger.info(GET_LOG + id);
    return verificationLogRepository.findById(id);
  }
}
