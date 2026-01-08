package org.lab1.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Optional;
import org.lab1.exception.NotFoundException;
import org.lab1.exception.ValidationException;
import org.lab1.json.MonetizationEvent;
import org.lab1.model.InAppAdd;
import org.lab1.model.InAppPurchase;
import org.lab1.model.MonetizedApplication;
import org.lab1.model.User;
import org.lab1.repository.InAppAddRepository;
import org.lab1.repository.InAppPurchaseRepository;
import org.lab1.repository.MonetizedApplicationRepository;
import org.lab1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
public class UserMonetizationService {
  private static final double INSUFFICIENT_BALANCE_PROBABILITY = 0.1;
  private static final String CARD_NUMBER_REGEX = "^\\d{16}$";
  private static final String CARD_HOLDER_REGEX = "^[a-zA-Z\\s]{3,}$";
  private static final String EXPIRY_DATE_REGEX = "^\\d{2}\\.\\d{2}\\.\\d{4}$";
  private static final String CVV_REGEX = "^\\d{3,4}$";
  private static final String REVENUE_SOURCE_METRIC = "revenue.source";
  private static final String TYPE_TAG = "type";
  private static final String DOWNLOAD_TYPE = "download";
  private static final String PURCHASE_TYPE = "purchase";
  private static final String AD_TYPE = "ad";
  private static final String REVENUE_AMOUNT_METRIC = "revenue.amount";
  private static final String USD_UNIT = "USD";
  private static final String USER_NOT_FOUND_MSG = "User not found";
  private static final String INVALID_CARD_MSG = "Invalid card";
  private static final String APP_NOT_FOUND_MSG = "Application not found";
  private static final String INSUFFICIENT_BALANCE_MSG = "Insufficient card balance";
  private static final String IN_APP_PURCHASE_NOT_FOUND_MSG = "In-app purchase item not found";

  private final MonetizedApplicationRepository monetizedApplicationRepository;
  private final InAppPurchaseRepository inAppPurchaseRepository;
  private final InAppAddRepository inAppAddRepository;
  private final UserRepository userRepository;
  private final PlatformTransactionManager transactionManager;
  private final Counter downloadRevenueCounter;
  private final Counter purchaseRevenueCounter;
  private final Counter adRevenueCounter;
  private final DistributionSummary downloadAmountSummary;
  private final DistributionSummary purchaseAmountSummary;
  private final DistributionSummary adAmountSummary;

  @Autowired
  public UserMonetizationService(
      final MonetizedApplicationRepository monetizedApplicationRepositoryParam,
      final InAppPurchaseRepository inAppPurchaseRepositoryParam,
      final InAppAddRepository inAppAddRepositoryParam,
      final UserRepository userRepositoryParam,
      final PlatformTransactionManager transactionManagerParam,
      final MeterRegistry meterRegistry) {
    this.monetizedApplicationRepository = monetizedApplicationRepositoryParam;
    this.inAppPurchaseRepository = inAppPurchaseRepositoryParam;
    this.inAppAddRepository = inAppAddRepositoryParam;
    this.userRepository = userRepositoryParam;
    this.transactionManager = transactionManagerParam;

    this.downloadRevenueCounter =
        Counter.builder(REVENUE_SOURCE_METRIC).tag(TYPE_TAG, DOWNLOAD_TYPE).register(meterRegistry);
    this.purchaseRevenueCounter =
        Counter.builder(REVENUE_SOURCE_METRIC).tag(TYPE_TAG, PURCHASE_TYPE).register(meterRegistry);
    this.adRevenueCounter =
        Counter.builder(REVENUE_SOURCE_METRIC).tag(TYPE_TAG, AD_TYPE).register(meterRegistry);
    this.downloadAmountSummary =
        DistributionSummary.builder(REVENUE_AMOUNT_METRIC)
            .tag(TYPE_TAG, DOWNLOAD_TYPE)
            .baseUnit(USD_UNIT)
            .register(meterRegistry);
    this.purchaseAmountSummary =
        DistributionSummary.builder(REVENUE_AMOUNT_METRIC)
            .tag(TYPE_TAG, PURCHASE_TYPE)
            .baseUnit(USD_UNIT)
            .register(meterRegistry);
    this.adAmountSummary =
        DistributionSummary.builder(REVENUE_AMOUNT_METRIC)
            .tag(TYPE_TAG, AD_TYPE)
            .baseUnit(USD_UNIT)
            .register(meterRegistry);
  }

  public final boolean downloadApplication(
      final int applicationId,
      final int userId,
      final String cardNumber,
      final String cardHolderName,
      final String expiryDate,
      final String cvv) {
    TransactionDefinition definition = new DefaultTransactionDefinition();
    TransactionStatus status = transactionManager.getTransaction(definition);

    try {
      Optional<User> userOptional = userRepository.findById(userId);
      if (userOptional.isEmpty()) {
        throw new NotFoundException(USER_NOT_FOUND_MSG);
      }

      User user = userOptional.get();

      if (!isValidCard(cardNumber, cardHolderName, expiryDate, cvv)) {
        throw new ValidationException(INVALID_CARD_MSG);
      }

      MonetizedApplication monetizedApplication =
          monetizedApplicationRepository.findByApplicationId(applicationId);
      if (monetizedApplication == null) {
        throw new NotFoundException(APP_NOT_FOUND_MSG);
      }

      if (Math.random() < INSUFFICIENT_BALANCE_PROBABILITY) {
        throw new ValidationException(INSUFFICIENT_BALANCE_MSG);
      }

      monetizedApplication.setCurrentBalance(
          monetizedApplication.getCurrentBalance()
              + monetizedApplication.getApplication().getPrice());
      monetizedApplication.setDownloadRevenue(
          monetizedApplication.getDownloadRevenue()
              + monetizedApplication.getApplication().getPrice());
      monetizedApplication.setRevenue(
          monetizedApplication.getRevenue() + monetizedApplication.getApplication().getPrice());

      userRepository.save(user);
      monetizedApplicationRepository.save(monetizedApplication);
      downloadRevenueCounter.increment();
      downloadAmountSummary.record(monetizedApplication.getApplication().getPrice());
      transactionManager.commit(status);

      return true;
    } catch (Exception exception) {
      transactionManager.rollback(status);
      throw exception;
    }
  }

  public final boolean purchaseInAppItem(
      final int purchaseId,
      final int userId,
      final String cardNumber,
      final String cardHolderName,
      final String expiryDate,
      final String cvv) {
    TransactionDefinition definition = new DefaultTransactionDefinition();
    TransactionStatus status = transactionManager.getTransaction(definition);

    try {
      Optional<User> userOptional = userRepository.findById(userId);
      if (userOptional.isEmpty()) {
        throw new NotFoundException(USER_NOT_FOUND_MSG);
      }

      User user = userOptional.get();

      if (!isValidCard(cardNumber, cardHolderName, expiryDate, cvv)) {
        throw new ValidationException(INVALID_CARD_MSG);
      }

      Optional<InAppPurchase> purchaseOptional = inAppPurchaseRepository.findById(purchaseId);
      if (purchaseOptional.isEmpty()) {
        throw new NotFoundException(IN_APP_PURCHASE_NOT_FOUND_MSG);
      }

      if (Math.random() < INSUFFICIENT_BALANCE_PROBABILITY) {
        throw new ValidationException(INSUFFICIENT_BALANCE_MSG);
      }

      InAppPurchase purchase = purchaseOptional.get();
      MonetizedApplication monetizedApplication = purchase.getMonetizedApplication();

      monetizedApplication.setCurrentBalance(
          monetizedApplication.getCurrentBalance() + purchase.getPrice());
      monetizedApplication.setPurchasesRevenue(
          monetizedApplication.getPurchasesRevenue() + purchase.getPrice());
      monetizedApplication.setRevenue(monetizedApplication.getRevenue() + purchase.getPrice());

      userRepository.save(user);
      monetizedApplicationRepository.save(monetizedApplication);
      purchaseRevenueCounter.increment();
      purchaseAmountSummary.record(purchase.getPrice());
      transactionManager.commit(status);
      return true;
    } catch (Exception exception) {
      transactionManager.rollback(status);
      throw exception;
    }
  }

  private boolean isValidCard(
      final String cardNumber,
      final String cardHolderName,
      final String expiryDate,
      final String cvv) {
    return cardNumber.matches(CARD_NUMBER_REGEX)
        && cardHolderName.matches(CARD_HOLDER_REGEX)
        && expiryDate.matches(EXPIRY_DATE_REGEX)
        && cvv.matches(CVV_REGEX);
  }

  public final boolean viewAdvertisement(final int adId) {
    Optional<InAppAdd> adOptional = inAppAddRepository.findById(adId);
    if (adOptional.isEmpty()) {
      throw new NotFoundException("InAppAdd not found with ID: " + adId);
    }

    InAppAdd ad = adOptional.get();
    MonetizedApplication monetizedApplication = ad.getMonetizedApplication();
    monetizedApplication.setCurrentBalance(
        monetizedApplication.getCurrentBalance() + ad.getPrice());
    monetizedApplication.setAdsRevenue(monetizedApplication.getAdsRevenue() + ad.getPrice());
    monetizedApplication.setRevenue(monetizedApplication.getRevenue() + ad.getPrice());

    adRevenueCounter.increment();
    adAmountSummary.record(ad.getPrice());
    monetizedApplicationRepository.save(monetizedApplication);
    return true;
  }

  public final InAppPurchase getInAppPurchaseById(final int purchaseId) {
    return inAppPurchaseRepository
        .findById(purchaseId)
        .orElseThrow(() -> new NotFoundException(IN_APP_PURCHASE_NOT_FOUND_MSG));
  }

  public final InAppAdd getInAppAddById(final int adId) {
    return inAppAddRepository
        .findById(adId)
        .orElseThrow(() -> new NotFoundException("InAppAdd not found with ID: " + adId));
  }

  public final MonetizationEvent createPurchaseEvent(
      final int userId, final int purchaseId, final double price) {
    InAppPurchase purchase = getInAppPurchaseById(purchaseId);
    int applicationId = purchase.getMonetizedApplication().getApplication().getId();

    return new MonetizationEvent(
        MonetizationEvent.EventType.PURCHASE, userId, applicationId, purchaseId, price);
  }

  public final MonetizationEvent createAdViewEvent(
      final int userId, final int adId, final double revenue) {
    InAppAdd ad = getInAppAddById(adId);
    int applicationId = ad.getMonetizedApplication().getApplication().getId();

    return new MonetizationEvent(
        MonetizationEvent.EventType.AD_VIEW, userId, applicationId, adId, revenue);
  }

  public final MonetizationEvent createDownloadEvent(
      final int userId, final int applicationId, final double price) {
    return new MonetizationEvent(
        MonetizationEvent.EventType.DOWNLOAD, userId, applicationId, applicationId, price);
  }
}
