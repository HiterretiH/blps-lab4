package org.lab1.service;

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

    @Autowired
    private MonetizedApplicationRepository monetizedApplicationRepository;

    @Autowired
    private InAppPurchaseRepository inAppPurchaseRepository;

    @Autowired
    private InAppAddRepository inAppAddRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationStatsRepository applicationStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Secured("ROLE_USER")
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

            transactionManager.commit(status);
            return true;
        } catch (Exception ex) {
            transactionManager.rollback(status);
            return false;
        }
    }

    @Secured("ROLE_USER")
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

        monetizedApplicationRepository.save(monetizedApplication);
        return true;
    }

}
