package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.model.Application;
import org.lab1.model.InAppAdd;
import org.lab1.model.InAppPurchaseLog;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.InAppAddRepository;
import org.lab1.repository.InAppPurchaseLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InAppPurchaseLogService {
    @Autowired
    private InAppPurchaseLogRepository inAppPurchaseLogRepository;
    @Autowired
    private Logger logger;

    public List<InAppPurchaseLog> getAllPlayers() {
        logger.info("Fetching all InAppPurchaseLogs");
        return inAppPurchaseLogRepository.findAll();
    }
}