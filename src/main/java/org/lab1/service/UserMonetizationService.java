package org.lab1.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.lab1.json.Card;
import org.lab1.model.*;
import org.lab1.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UserMonetizationService {

    private final MonetizedApplicationRepository monetizedApplicationRepository;
    private final InAppPurchaseRepository inAppPurchaseRepository;
    private final InAppAddRepository inAppAddRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatsRepository applicationStatsRepository;
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
            MonetizedApplicationRepository monetizedApplicationRepository,
            InAppPurchaseRepository inAppPurchaseRepository,
            InAppAddRepository inAppAddRepository,
            ApplicationRepository applicationRepository,
            ApplicationStatsRepository applicationStatsRepository,
            UserRepository userRepository,
            PlatformTransactionManager transactionManager,
            MeterRegistry meterRegistry) {
        this.monetizedApplicationRepository = monetizedApplicationRepository;
        this.inAppPurchaseRepository = inAppPurchaseRepository;
        this.inAppAddRepository = inAppAddRepository;
        this.applicationRepository = applicationRepository;
        this.applicationStatsRepository = applicationStatsRepository;
        this.userRepository = userRepository;
        this.transactionManager = transactionManager;

        this.downloadRevenueCounter = Counter.builder("revenue.source")
                .tag("type", "download")
                .register(meterRegistry);
        this.purchaseRevenueCounter = Counter.builder("revenue.source")
                .tag("type", "purchase")
                .register(meterRegistry);
        this.adRevenueCounter = Counter.builder("revenue.source")
                .tag("type", "ad")
                .register(meterRegistry);
        this.downloadAmountSummary = DistributionSummary.builder("revenue.amount")
                .tag("type", "download")
                .baseUnit("USD")
                .register(meterRegistry);
        this.purchaseAmountSummary = DistributionSummary.builder("revenue.amount")
                .tag("type", "purchase")
                .baseUnit("USD")
                .register(meterRegistry);
        this.adAmountSummary = DistributionSummary.builder("revenue.amount")
                .tag("type", "ad")
                .baseUnit("USD")
                .register(meterRegistry);
    }

    public boolean downloadApplication(int applicationId, int userId, String cardNumber, String cardHolderName, String expiryDate, String cvv) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
            }

            User user = userOpt.get();

            if (!isValidCard(cardNumber, cardHolderName, expiryDate, cvv)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card");
            }

            MonetizedApplication monetizedApplication = monetizedApplicationRepository.findByApplicationId(applicationId);
            if (monetizedApplication == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application not found");
            }

            if (Math.random() < 0.1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient card balance");
            }

            monetizedApplication.setCurrentBalance(monetizedApplication.getCurrentBalance() + monetizedApplication.getApplication().getPrice());
            monetizedApplication.setDownloadRevenue(monetizedApplication.getDownloadRevenue() + monetizedApplication.getApplication().getPrice());
            monetizedApplication.setRevenue(monetizedApplication.getRevenue() + monetizedApplication.getApplication().getPrice());

            userRepository.save(user);
            monetizedApplicationRepository.save(monetizedApplication);
downloadRevenueCounter.increment();
        downloadAmountSummary.record(monetizedApplication.getApplication().getPrice());
            transactionManager.commit(status);

            return true;
        } catch (Exception ex) {
            transactionManager.rollback(status);
            return false;
        }
    }

    public boolean purchaseInAppItem(int purchaseId, int userId, String cardNumber, String cardHolderName, String expiryDate, String cvv) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
            }

            User user = userOpt.get();

            if (!isValidCard(cardNumber, cardHolderName, expiryDate, cvv)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card");
            }

            Optional<InAppPurchase> purchaseOpt = inAppPurchaseRepository.findById(purchaseId);
            if (purchaseOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "In-app purchase item not found");
            }

            if (Math.random() < 0.1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient card balance");
            }

            InAppPurchase purchase = purchaseOpt.get();
            MonetizedApplication monetizedApplication = purchase.getMonetizedApplication();

            monetizedApplication.setCurrentBalance(monetizedApplication.getCurrentBalance() + purchase.getPrice());
            monetizedApplication.setPurchasesRevenue(monetizedApplication.getPurchasesRevenue() + purchase.getPrice());
            monetizedApplication.setRevenue(monetizedApplication.getRevenue() + purchase.getPrice());

            userRepository.save(user);
            monetizedApplicationRepository.save(monetizedApplication);
            purchaseRevenueCounter.increment();
            purchaseAmountSummary.record(purchase.getPrice());
            transactionManager.commit(status);
            return true;
        } catch (Exception ex) {
            transactionManager.rollback(status);
            return false;
        }
    }

    private boolean isValidCard(String cardNumber, String cardHolderName, String expiryDate, String cvv) {
        return cardNumber.matches("^\\d{16}$") &&
                cardHolderName.matches("^[a-zA-Z\\s]{3,}$") &&
                expiryDate.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$") &&
                cvv.matches("^\\d{3,4}$");
    }

    public boolean viewAdvertisement(int adId) {
        Optional<InAppAdd> adOpt = inAppAddRepository.findById(adId);
        if (adOpt.isEmpty()) {
            return false;
        }

        InAppAdd ad = adOpt.get();
        MonetizedApplication monetizedApplication = ad.getMonetizedApplication();
        monetizedApplication.setCurrentBalance(monetizedApplication.getCurrentBalance() + ad.getPrice());
        monetizedApplication.setAdsRevenue(monetizedApplication.getAdsRevenue() + ad.getPrice());
        monetizedApplication.setRevenue(monetizedApplication.getRevenue() + ad.getPrice());
        adRevenueCounter.increment();
        adAmountSummary.record(ad.getPrice());
        monetizedApplicationRepository.save(monetizedApplication);
        return true;
    }

}
