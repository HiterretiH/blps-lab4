package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.model.VerificationLog;
import org.lab1.repository.VerificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VerificationLogService {

    private final VerificationLogRepository verificationLogRepository;
    @Autowired
    private Logger logger;

    @Autowired
    public VerificationLogService(VerificationLogRepository verificationLogRepository) {
        this.verificationLogRepository = verificationLogRepository;
    }

    public VerificationLog createVerificationLog(boolean securityCheckPassed, boolean policyCheckPassed, boolean adsCheckPassed, String logMessage) {
        logger.info("Creating VerificationLog");
        VerificationLog verificationLog = new VerificationLog();
        verificationLog.setSecurityCheckPassed(securityCheckPassed);
        verificationLog.setPolicyCheckPassed(policyCheckPassed);
        verificationLog.setAdsCheckPassed(adsCheckPassed);
        verificationLog.setLogMessage(logMessage);
        return verificationLogRepository.save(verificationLog);
    }

    public Optional<VerificationLog> getVerificationLogById(int id) {
        logger.info("Getting VerificationLog by ID: " + id);
        return verificationLogRepository.findById(id);
    }
}