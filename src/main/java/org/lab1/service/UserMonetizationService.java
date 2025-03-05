package org.lab1.service;

import org.lab1.model.Application;
import org.lab1.model.ApplicationStats;
import org.lab1.model.InAppAdd;
import org.lab1.model.InAppPurchase;
import org.lab1.model.MonetizedApplication;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.ApplicationStatsRepository;
import org.lab1.repository.InAppAddRepository;
import org.lab1.repository.InAppPurchaseRepository;
import org.lab1.repository.MonetizedApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public boolean downloadApplication(int applicationId) {
        MonetizedApplication monetizedApplication = monetizedApplicationRepository.findByApplicationId(applicationId);
        if (monetizedApplication == null) {
            return false;
        }

        Application application = monetizedApplication.getApplication();
        monetizedApplication.setCurrentBalance(monetizedApplication.getCurrentBalance() + application.getPrice());
        monetizedApplication.setDownloadRevenue(monetizedApplication.getDownloadRevenue() + application.getPrice());
        monetizedApplication.setRevenue(monetizedApplication.getRevenue() + application.getPrice());

        ApplicationStats applicationStats = applicationStatsRepository.findByApplicationId(applicationId);

        if (applicationStats != null) {
            applicationStats.setDownloads(applicationStats.getDownloads() + 1);
            applicationStatsRepository.save(applicationStats);
        }

        monetizedApplicationRepository.save(monetizedApplication);
        return true;
    }

    public boolean purchaseInAppItem(int purchaseId) {
        Optional<InAppPurchase> purchaseOpt = inAppPurchaseRepository.findById(purchaseId);
        if (purchaseOpt.isEmpty()) {
            return false;
        }

        InAppPurchase purchase = purchaseOpt.get();
        MonetizedApplication monetizedApplication = purchase.getMonetizedApplication();
        monetizedApplication.setCurrentBalance(monetizedApplication.getCurrentBalance() + purchase.getPrice());
        monetizedApplication.setPurchasesRevenue(monetizedApplication.getPurchasesRevenue() + purchase.getPrice());
        monetizedApplication.setRevenue(monetizedApplication.getRevenue() + purchase.getPrice());

        monetizedApplicationRepository.save(monetizedApplication);
        return true;
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
