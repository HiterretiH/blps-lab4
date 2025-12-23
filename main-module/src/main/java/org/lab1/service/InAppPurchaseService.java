package org.lab1.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

@Service
public class InAppPurchaseService {
  private static final String CREATE_REQUEST_LOG = "Creating InAppPurchases. Titles count: ";
  private static final String DESCRIPTIONS_COUNT_LOG = ", Descriptions count: ";
  private static final String PRICES_COUNT_LOG = ", Prices count: ";
  private static final String SAME_COUNT_ERROR =
      "The number of titles, prices, and descriptions must be the same.";
  private static final String CREATED_PURCHASE_LOG = "Created InAppPurchase: ";
  private static final String WITH_ID_LOG = " with ID: ";
  private static final String TRANSACTION_COMMITTED_LOG = "Transaction committed. Created ";
  private static final String IN_APP_PURCHASES_LOG = " InAppPurchases.";
  private static final String CREATION_ERROR_LOG = "Error during InAppPurchases creation. Reason: ";
  private static final String FETCH_ALL_LOG = "Fetching all InAppPurchases.";
  private static final String FOUND_ALL_LOG = "Found ";
  private static final String FETCH_BY_ID_LOG = "Fetching InAppPurchase by ID: ";
  private static final String FOUND_BY_ID_LOG = "InAppPurchase found with ID: ";
  private static final String TITLE_LOG = ", title: ";
  private static final String NOT_FOUND_BY_ID_LOG = "InAppPurchase not found with ID: ";
  private static final String LINK_REQUEST_LOG =
      "Linking InAppPurchases to MonetizedApplication ID: ";
  private static final String MONETIZED_APP_NOT_FOUND_LOG =
      "Monetized application not found with ID: ";
  private static final String MONETIZED_APP_NOT_FOUND_MSG = "Monetized application not found";
  private static final String FOUND_NULL_PURCHASES_LOG = "Found ";
  private static final String NULL_PURCHASES_LOG =
      " InAppPurchases with null MonetizedApplication.";
  private static final String LINKED_PURCHASE_LOG = "Linked InAppPurchase ID: ";
  private static final String TO_APP_ID_LOG = " to MonetizedApplication ID: ";
  private static final String LINK_SUCCESS_LOG = "Successfully linked ";
  private static final String PURCHASES_TO_APP_LOG = " InAppPurchases to MonetizedApplication ID: ";

  private final InAppPurchaseRepository inAppPurchaseRepository;
  private final MonetizedApplicationRepository monetizedApplicationRepository;
  private final JtaTransactionManager transactionManager;
  private final Logger logger;

  @Autowired
  public InAppPurchaseService(
      final InAppPurchaseRepository inAppPurchaseRepository,
      final MonetizedApplicationRepository monetizedApplicationRepository,
      final JtaTransactionManager transactionManager,
      final Logger logger) {
    this.inAppPurchaseRepository = inAppPurchaseRepository;
    this.monetizedApplicationRepository = monetizedApplicationRepository;
    this.transactionManager = transactionManager;
    this.logger = logger;
  }

  public final List<InAppPurchase> createInAppPurchases(
      final List<String> titles, final List<String> descriptions, final List<Double> prices) {
    int titlesCount = titles != null ? titles.size() : 0;
    int descriptionsCount = descriptions != null ? descriptions.size() : 0;
    int pricesCount = prices != null ? prices.size() : 0;

    logger.info(
        CREATE_REQUEST_LOG
            + titlesCount
            + DESCRIPTIONS_COUNT_LOG
            + descriptionsCount
            + PRICES_COUNT_LOG
            + pricesCount);

    TransactionDefinition definition = new DefaultTransactionDefinition();
    TransactionStatus status = transactionManager.getTransaction(definition);
    List<InAppPurchase> purchases = new ArrayList<>();

    try {
      if (titles == null
          || descriptions == null
          || prices == null
          || titles.size() != prices.size()
          || descriptions.size() != prices.size()) {
        transactionManager.rollback(status);
        logger.error(SAME_COUNT_ERROR);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SAME_COUNT_ERROR);
      }

      for (int i = 0; i < titles.size(); i++) {
        InAppPurchase purchase = new InAppPurchase();
        purchase.setTitle(titles.get(i));
        purchase.setDescription(descriptions.get(i));
        purchase.setPrice(prices.get(i));
        purchase.setMonetizedApplication(null);
        inAppPurchaseRepository.save(purchase);
        purchases.add(purchase);
        logger.info(CREATED_PURCHASE_LOG + purchase.getTitle() + WITH_ID_LOG + purchase.getId());
      }

      transactionManager.commit(status);
      logger.info(TRANSACTION_COMMITTED_LOG + purchases.size() + IN_APP_PURCHASES_LOG);

    } catch (Exception exception) {
      transactionManager.rollback(status);
      logger.error(CREATION_ERROR_LOG + exception.getMessage());
      throw exception;
    }

    return purchases;
  }

  public final List<InAppPurchase> getAllInAppPurchases() {
    logger.info(FETCH_ALL_LOG);
    List<InAppPurchase> purchases = inAppPurchaseRepository.findAll();
    logger.info(FOUND_ALL_LOG + purchases.size() + IN_APP_PURCHASES_LOG);
    return purchases;
  }

  public final Optional<InAppPurchase> getInAppPurchaseById(final int id) {
    logger.info(FETCH_BY_ID_LOG + id);
    Optional<InAppPurchase> purchase = inAppPurchaseRepository.findById(id);

    if (purchase.isPresent()) {
      logger.info(FOUND_BY_ID_LOG + id + TITLE_LOG + purchase.get().getTitle());
    } else {
      logger.info(NOT_FOUND_BY_ID_LOG + id);
    }

    return purchase;
  }

  public final List<InAppPurchase> linkMonetizedAppToPurchases(final int monetizedApplicationId) {
    logger.info(LINK_REQUEST_LOG + monetizedApplicationId);

    MonetizedApplication monetizedApplication =
        monetizedApplicationRepository
            .findById(monetizedApplicationId)
            .orElseThrow(
                () -> {
                  logger.error(MONETIZED_APP_NOT_FOUND_LOG + monetizedApplicationId);
                  return new ResponseStatusException(
                      HttpStatus.NOT_FOUND, MONETIZED_APP_NOT_FOUND_MSG);
                });

    List<InAppPurchase> purchases = inAppPurchaseRepository.findByMonetizedApplicationNull();
    logger.info(FOUND_NULL_PURCHASES_LOG + purchases.size() + NULL_PURCHASES_LOG);

    for (InAppPurchase purchase : purchases) {
      purchase.setMonetizedApplication(monetizedApplication);
      inAppPurchaseRepository.save(purchase);
      logger.info(LINKED_PURCHASE_LOG + purchase.getId() + TO_APP_ID_LOG + monetizedApplicationId);
    }

    logger.info(
        LINK_SUCCESS_LOG + purchases.size() + PURCHASES_TO_APP_LOG + monetizedApplicationId);
    return purchases;
  }
}
