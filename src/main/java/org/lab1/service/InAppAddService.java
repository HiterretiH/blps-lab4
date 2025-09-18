package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.json.InAppAddJson;
import org.lab1.model.InAppAdd;
import org.lab1.model.MonetizedApplication;
import org.lab1.repository.InAppAddRepository;
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
public class InAppAddService {

    private final InAppAddRepository inAppAddRepository;
    private final MonetizedApplicationRepository monetizedApplicationRepository;
    private final JtaTransactionManager transactionManager;
    private final Logger logger;

    @Autowired
    public InAppAddService(InAppAddRepository inAppAddRepository, MonetizedApplicationRepository monetizedApplicationRepository, JtaTransactionManager transactionManager, Logger logger) {
        this.inAppAddRepository = inAppAddRepository;
        this.monetizedApplicationRepository = monetizedApplicationRepository;
        this.transactionManager = transactionManager;
        this.logger = logger;
    }

    public InAppAdd createInAppAdd(InAppAddJson inAppAddJson) {
        logger.info("Creating InAppAdd for MonetizedApplication ID: " + inAppAddJson.getMonetizedApplicationId());
        MonetizedApplication monetizedApplication = monetizedApplicationRepository.findById(inAppAddJson.getMonetizedApplicationId())
                .orElseThrow(() -> {
                    logger.error("Monetized Application not found with ID: " + inAppAddJson.getMonetizedApplicationId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Monetized Application not found");
                });
        InAppAdd inAppAdd = new InAppAdd();
        inAppAdd.setMonetizedApplication(monetizedApplication);
        inAppAdd.setTitle(inAppAddJson.getTitle());
        inAppAdd.setDescription(inAppAddJson.getDescription());
        inAppAdd.setPrice(inAppAddJson.getPrice());
        InAppAdd savedInAppAdd = inAppAddRepository.save(inAppAdd);
        logger.info("InAppAdd created with ID: " + savedInAppAdd.getId() + " for MonetizedApplication ID: " + monetizedApplication.getId());
        return savedInAppAdd;
    }

    public List<InAppAdd> createMultipleInAppAdds(List<InAppAddJson> inAppAddJsons) {
        logger.info("Creating multiple InAppAdds. Count: " + (inAppAddJsons != null ? inAppAddJsons.size() : 0));
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        List<InAppAdd> inAppAdds = new ArrayList<>();
        try {
            if (inAppAddJsons == null || inAppAddJsons.isEmpty()) {
                logger.info("No InAppAdds to create.");
                return inAppAdds;
            }
            Integer monetizedApplicationId = inAppAddJsons.get(0).getMonetizedApplicationId();
            MonetizedApplication monetizedApplication = monetizedApplicationRepository.findById(monetizedApplicationId)
                    .orElseThrow(() -> {
                        logger.error("Monetized Application not found with ID: " + monetizedApplicationId + " for bulk create.");
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Monetized Application not found");
                    });
            for (InAppAddJson inAppAddJson : inAppAddJsons) {
                if (inAppAddJson.getMonetizedApplicationId() != monetizedApplicationId) {
                    transactionManager.rollback(status);
                    logger.error("All ads must belong to the same Monetized Application for bulk create. Found different ID: " + inAppAddJson.getMonetizedApplicationId() + ", expected: " + monetizedApplicationId);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All ads must belong to the same Monetized Application for a single transaction.");
                }
                InAppAdd inAppAdd = new InAppAdd();
                inAppAdd.setMonetizedApplication(monetizedApplication);
                inAppAdd.setTitle(inAppAddJson.getTitle());
                inAppAdd.setDescription(inAppAddJson.getDescription());
                inAppAdd.setPrice(inAppAddJson.getPrice());
                inAppAdds.add(inAppAdd);
            }
            inAppAddRepository.saveAll(inAppAdds);
            transactionManager.commit(status);
            logger.info("Successfully created " + inAppAdds.size() + " InAppAdds for MonetizedApplication ID: " + monetizedApplicationId);
        } catch (Exception ex) {
            transactionManager.rollback(status);
            logger.error("Error during bulk creation of InAppAdds. Reason: " + ex.getMessage());
            throw ex;
        }
        return inAppAdds;
    }

    public List<InAppAdd> getAllInAppAds() {
        logger.info("Fetching all InAppAdds.");
        List<InAppAdd> inAppAdds = inAppAddRepository.findAll();
        logger.info("Found " + inAppAdds.size() + " InAppAdds.");
        return inAppAdds;
    }

    public Optional<InAppAdd> getInAppAddById(int id) {
        logger.info("Fetching InAppAdd by ID: " + id);
        Optional<InAppAdd> inAppAdd = inAppAddRepository.findById(id);
        if (inAppAdd.isPresent()) {
            logger.info("InAppAdd found with ID: " + id);
        } else {
            logger.info("InAppAdd not found with ID: " + id);
        }
        return inAppAdd;
    }

    public List<InAppAdd> getInAppAddByMonetizedApplication(int monetizedApplicationId) {
        logger.info("Fetching InAppAdds by MonetizedApplication ID: " + monetizedApplicationId);
        List<InAppAdd> inAppAdds = inAppAddRepository.findByMonetizedApplicationId(monetizedApplicationId);
        logger.info("Found " + inAppAdds.size() + " InAppAdds for MonetizedApplication ID: " + monetizedApplicationId);
        return inAppAdds;
    }
}