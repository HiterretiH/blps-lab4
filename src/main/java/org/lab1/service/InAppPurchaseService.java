package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.model.InAppPurchase;
import org.lab1.model.MonetizedApplication;
import org.lab1.repository.InAppPurchaseRepository;
import org.lab1.repository.MonetizedApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InAppPurchaseService {
    private final InAppPurchaseRepository inAppPurchaseRepository;
    private final MonetizedApplicationRepository monetizedApplicationRepository;
    private final JtaTransactionManager transactionManager;
    private final Logger logger;

    @Autowired
    public InAppPurchaseService(InAppPurchaseRepository inAppPurchaseRepository, MonetizedApplicationRepository monetizedApplicationRepository, JtaTransactionManager transactionManager, Logger logger) {
        this.inAppPurchaseRepository = inAppPurchaseRepository;
        this.monetizedApplicationRepository = monetizedApplicationRepository;
        this.transactionManager = transactionManager;
        this.logger = logger;
    }

    public List<InAppPurchase> createInAppPurchases(List<String> titles, List<String> descriptions, List<Double> prices) {
        logger.info("Creating InAppPurchases. Titles count: " + (titles != null ? titles.size() : 0) +
                ", Descriptions count: " + (descriptions != null ? descriptions.size() : 0) +
                ", Prices count: " + (prices != null ? prices.size() : 0));
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        List<InAppPurchase> purchases = new ArrayList<>();
        try {
            if (titles == null || descriptions == null || prices == null || titles.size() != prices.size() || descriptions.size() != prices.size()) {
                transactionManager.rollback(status);
                logger.error("The number of titles, prices, and descriptions must be the same.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The number of titles, prices, and descriptions must be the same.");
            }
            for (int i = 0; i < titles.size(); i++) {
                InAppPurchase purchase = new InAppPurchase();
                purchase.setTitle(titles.get(i));
                purchase.setDescription(descriptions.get(i));
                purchase.setPrice(prices.get(i));
                purchase.setMonetizedApplication(null);
                inAppPurchaseRepository.save(purchase);
                purchases.add(purchase);
                logger.info("Created InAppPurchase: " + purchase.getTitle() + " with ID: " + purchase.getId());
            }
            transactionManager.commit(status);
            logger.info("Transaction committed. Created " + purchases.size() + " InAppPurchases.");
        } catch (Exception ex) {
            transactionManager.rollback(status);
            logger.error("Error during InAppPurchases creation. Reason: " + ex.getMessage());
            throw ex;
        }
        return purchases;
    }

    public List<InAppPurchase> getAllInAppPurchases() {
        logger.info("Fetching all InAppPurchases.");
        List<InAppPurchase> purchases = inAppPurchaseRepository.findAll();
        logger.info("Found " + purchases.size() + " InAppPurchases.");
        return purchases;
    }

    public Optional<InAppPurchase> getInAppPurchaseById(int id) {
        logger.info("Fetching InAppPurchase by ID: " + id);
        Optional<InAppPurchase> purchase = inAppPurchaseRepository.findById(id);
        if (purchase.isPresent()) {
            logger.info("InAppPurchase found with ID: " + id + ", title: " + purchase.get().getTitle());
        } else {
            logger.info("InAppPurchase not found with ID: " + id);
        }
        return purchase;
    }

    public List<InAppPurchase> linkMonetizedAppToPurchases(int monetizedApplicationId) {
        logger.info("Linking InAppPurchases to MonetizedApplication ID: " + monetizedApplicationId);
        MonetizedApplication monetizedApplication = monetizedApplicationRepository.findById(monetizedApplicationId)
                .orElseThrow(() -> {
                    logger.error("Monetized application not found with ID: " + monetizedApplicationId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Monetized application not found");
                });
        List<InAppPurchase> purchases = inAppPurchaseRepository.findByMonetizedApplicationNull();
        logger.info("Found " + purchases.size() + " InAppPurchases with null MonetizedApplication.");
        for (InAppPurchase purchase : purchases) {
            purchase.setMonetizedApplication(monetizedApplication);
            inAppPurchaseRepository.save(purchase);
            logger.info("Linked InAppPurchase ID: " + purchase.getId() + " to MonetizedApplication ID: " + monetizedApplicationId);
        }
        logger.info("Successfully linked " + purchases.size() + " InAppPurchases to MonetizedApplication ID: " + monetizedApplicationId);
        return purchases;
    }
}