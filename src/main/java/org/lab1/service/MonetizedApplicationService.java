package org.lab1.service;

import org.lab1.json.MonetizedApplicationJson;
import org.lab1.model.Application;
import org.lab1.model.Developer;
import org.lab1.model.MonetizedApplication;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.DeveloperRepository;
import org.lab1.repository.MonetizedApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class MonetizedApplicationService {

    private final MonetizedApplicationRepository monetizedApplicationRepository;
    private final DeveloperRepository developerRepository;
    private final ApplicationRepository applicationRepository;

    @Autowired
    public MonetizedApplicationService(MonetizedApplicationRepository monetizedApplicationRepository, DeveloperRepository developerRepository, ApplicationRepository applicationRepository) {
        this.monetizedApplicationRepository = monetizedApplicationRepository;
        this.developerRepository = developerRepository;
        this.applicationRepository = applicationRepository;
    }

    public MonetizedApplication createMonetizedApplication(MonetizedApplicationJson monetizedApplicationJson) {
        Developer developer = developerRepository.findById(monetizedApplicationJson.getDeveloperId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer not found"));

        Application application = applicationRepository.findById(monetizedApplicationJson.getApplicationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        MonetizedApplication monetizedApplication = new MonetizedApplication();
        monetizedApplication.setDeveloper(developer);
        monetizedApplication.setApplication(application);
        monetizedApplication.setCurrentBalance(monetizedApplicationJson.getCurrentBalance());
        monetizedApplication.setRevenue(monetizedApplicationJson.getRevenue());
        monetizedApplication.setDownloadRevenue(monetizedApplicationJson.getDownloadRevenue());
        monetizedApplication.setAdsRevenue(monetizedApplicationJson.getAdsRevenue());
        monetizedApplication.setPurchasesRevenue(monetizedApplicationJson.getPurchasesRevenue());

        return monetizedApplicationRepository.save(monetizedApplication);
    }

    public Optional<MonetizedApplication> getMonetizedApplicationById(int id) {
        return monetizedApplicationRepository.findById(id);
    }
}