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
    private static final String CREATE_REQUEST_LOG = "Creating InAppAdd for MonetizedApplication ID: ";
    private static final String MONETIZED_APP_NOT_FOUND_LOG = "Monetized Application not found with ID: ";
    private static final String MONETIZED_APP_NOT_FOUND_MSG = "Monetized Application not found";
    private static final String CREATED_IN_APP_ADD_LOG = "InAppAdd created with ID: ";
    private static final String FOR_MONETIZED_APP_LOG = " for MonetizedApplication ID: ";
    private static final String BULK_CREATE_LOG = "Creating multiple InAppAdds. Count: ";
    private static final String NO_IN_APP_ADDS_LOG = "No InAppAdds to create.";
    private static final String BULK_MONETIZED_NOT_FOUND_LOG = "Monetized Application not found with ID: ";
    private static final String BULK_MONETIZED_NOT_FOUND_MSG = " for bulk create.";
    private static final String DIFFERENT_APP_ID_LOG = "All ads must belong to the same Monetized Application for bulk create. Found different ID: ";
    private static final String EXPECTED_ID_LOG = ", expected: ";
    private static final String BULK_SAME_APP_ERROR = "All ads must belong to the same Monetized Application for a single transaction.";
    private static final String BULK_CREATE_SUCCESS_LOG = "Successfully created ";
    private static final String IN_APP_ADDS_LOG = " InAppAdds for MonetizedApplication ID: ";
    private static final String BULK_CREATE_ERROR_LOG = "Error during bulk creation of InAppAdds. Reason: ";
    private static final String FETCH_ALL_LOG = "Fetching all InAppAdds.";
    private static final String FOUND_ALL_LOG = "Found ";
    private static final String FETCH_BY_ID_LOG = "Fetching InAppAdd by ID: ";
    private static final String FOUND_BY_ID_LOG = "InAppAdd found with ID: ";
    private static final String NOT_FOUND_BY_ID_LOG = "InAppAdd not found with ID: ";
    private static final String FETCH_BY_APP_LOG = "Fetching InAppAdds by MonetizedApplication ID: ";
    private static final String FOUND_BY_APP_LOG = "Found ";
    private static final String FOR_APP_ID_LOG = " InAppAdds for MonetizedApplication ID: ";

    private final InAppAddRepository inAppAddRepository;
    private final MonetizedApplicationRepository monetizedApplicationRepository;
    private final JtaTransactionManager transactionManager;
    private final Logger logger;

    @Autowired
    public InAppAddService(InAppAddRepository inAppAddRepository,
                           MonetizedApplicationRepository monetizedApplicationRepository,
                           JtaTransactionManager transactionManager, Logger logger) {
        this.inAppAddRepository = inAppAddRepository;
        this.monetizedApplicationRepository = monetizedApplicationRepository;
        this.transactionManager = transactionManager;
        this.logger = logger;
    }

    public InAppAdd createInAppAdd(InAppAddJson inAppAddJson) {
        logger.info(CREATE_REQUEST_LOG + inAppAddJson.getMonetizedApplicationId());
        MonetizedApplication monetizedApplication = monetizedApplicationRepository.findById(inAppAddJson.getMonetizedApplicationId())
                .orElseThrow(() -> {
                    logger.error(MONETIZED_APP_NOT_FOUND_LOG + inAppAddJson.getMonetizedApplicationId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, MONETIZED_APP_NOT_FOUND_MSG);
                });

        InAppAdd inAppAdd = new InAppAdd();
        inAppAdd.setMonetizedApplication(monetizedApplication);
        inAppAdd.setTitle(inAppAddJson.getTitle());
        inAppAdd.setDescription(inAppAddJson.getDescription());
        inAppAdd.setPrice(inAppAddJson.getPrice());

        InAppAdd savedInAppAdd = inAppAddRepository.save(inAppAdd);
        logger.info(CREATED_IN_APP_ADD_LOG + savedInAppAdd.getId() + FOR_MONETIZED_APP_LOG + monetizedApplication.getId());
        return savedInAppAdd;
    }

    public List<InAppAdd> createMultipleInAppAdds(List<InAppAddJson> inAppAddJsons) {
        int count = inAppAddJsons != null ? inAppAddJsons.size() : 0;
        logger.info(BULK_CREATE_LOG + count);

        TransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);
        List<InAppAdd> inAppAdds = new ArrayList<>();

        try {
            if (inAppAddJsons == null || inAppAddJsons.isEmpty()) {
                logger.info(NO_IN_APP_ADDS_LOG);
                return inAppAdds;
            }

            Integer monetizedApplicationId = inAppAddJsons.get(0).getMonetizedApplicationId();
            MonetizedApplication monetizedApplication = monetizedApplicationRepository.findById(monetizedApplicationId)
                    .orElseThrow(() -> {
                        logger.error(BULK_MONETIZED_NOT_FOUND_LOG + monetizedApplicationId + BULK_MONETIZED_NOT_FOUND_MSG);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, MONETIZED_APP_NOT_FOUND_MSG);
                    });

            for (InAppAddJson inAppAddJson : inAppAddJsons) {
                if (inAppAddJson.getMonetizedApplicationId() != monetizedApplicationId) {
                    transactionManager.rollback(status);
                    logger.error(DIFFERENT_APP_ID_LOG + inAppAddJson.getMonetizedApplicationId() +
                            EXPECTED_ID_LOG + monetizedApplicationId);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, BULK_SAME_APP_ERROR);
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
            logger.info(BULK_CREATE_SUCCESS_LOG + inAppAdds.size() + IN_APP_ADDS_LOG + monetizedApplicationId);

        } catch (Exception exception) {
            transactionManager.rollback(status);
            logger.error(BULK_CREATE_ERROR_LOG + exception.getMessage());
            throw exception;
        }

        return inAppAdds;
    }

    public List<InAppAdd> getAllInAppAds() {
        logger.info(FETCH_ALL_LOG);
        List<InAppAdd> inAppAdds = inAppAddRepository.findAll();
        logger.info(FOUND_ALL_LOG + inAppAdds.size() + IN_APP_ADDS_LOG);
        return inAppAdds;
    }

    public Optional<InAppAdd> getInAppAddById(int id) {
        logger.info(FETCH_BY_ID_LOG + id);
        Optional<InAppAdd> inAppAdd = inAppAddRepository.findById(id);

        if (inAppAdd.isPresent()) {
            logger.info(FOUND_BY_ID_LOG + id);
        } else {
            logger.info(NOT_FOUND_BY_ID_LOG + id);
        }

        return inAppAdd;
    }

    public List<InAppAdd> getInAppAddByMonetizedApplication(int monetizedApplicationId) {
        logger.info(FETCH_BY_APP_LOG + monetizedApplicationId);
        List<InAppAdd> inAppAdds = inAppAddRepository.findByMonetizedApplicationId(monetizedApplicationId);
        logger.info(FOUND_BY_APP_LOG + inAppAdds.size() + FOR_APP_ID_LOG + monetizedApplicationId);
        return inAppAdds;
    }
}
