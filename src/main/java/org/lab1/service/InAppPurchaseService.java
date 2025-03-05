package org.lab1.service;

import org.lab1.model.InAppPurchase;
import org.lab1.model.MonetizedApplication;
import org.lab1.repository.InAppPurchaseRepository;
import org.lab1.repository.MonetizedApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InAppPurchaseService {
    @Autowired
    private InAppPurchaseRepository inAppPurchaseRepository;

    @Autowired
    private MonetizedApplicationRepository monetizedApplicationRepository;

    public List<InAppPurchase> createInAppPurchases(List<String> titles, List<String> descriptions, List<Double> prices) {
        if (titles.size() != prices.size() || descriptions.size() != prices.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The number of titles, prices, and descriptions must be the same.");
        }

        List<InAppPurchase> purchases = new ArrayList<>();

        for (int i = 0; i < titles.size(); i++) {
            InAppPurchase purchase = new InAppPurchase();
            purchase.setTitle(titles.get(i));
            purchase.setDescription(descriptions.get(i));
            purchase.setPrice(prices.get(i));
            purchase.setMonetizedApplication(null);

            inAppPurchaseRepository.save(purchase);
            purchases.add(purchase);
        }

        return purchases;
    }

    public List<InAppPurchase> getAllInAppPurchases() {
        return inAppPurchaseRepository.findAll();
    }

    public Optional<InAppPurchase> getInAppPurchaseById(int id) {
        return inAppPurchaseRepository.findById(id);
    }

    public List<InAppPurchase> linkMonetizedAppToPurchases(int monetizedApplicationId) {
        MonetizedApplication monetizedApplication = monetizedApplicationRepository.findById(monetizedApplicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monetized application not found"));

        List<InAppPurchase> purchases = inAppPurchaseRepository.findByMonetizedApplicationNull();

        for (InAppPurchase purchase : purchases) {
            purchase.setMonetizedApplication(monetizedApplication);
            inAppPurchaseRepository.save(purchase);
        }
        return purchases;
    }
}
