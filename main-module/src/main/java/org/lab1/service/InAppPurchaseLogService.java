package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.model.InAppPurchaseLog;
import org.lab1.repository.InAppPurchaseLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InAppPurchaseLogService {
    private static final String FETCH_ALL_LOGS_LOG = "Fetching all InAppPurchaseLogs";

    @Autowired
    private InAppPurchaseLogRepository inAppPurchaseLogRepository;
    @Autowired
    private Logger logger;

    public List<InAppPurchaseLog> getAllPlayers() {
        logger.info(FETCH_ALL_LOGS_LOG);
        return inAppPurchaseLogRepository.findAll();
    }
}
