package org.lab1.service;

import org.lab.logger.Logger;
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
    private final Logger logger;

    @Autowired
    public MonetizedApplicationService(MonetizedApplicationRepository monetizedApplicationRepository, DeveloperRepository developerRepository, ApplicationRepository applicationRepository, Logger logger) {
        this.monetizedApplicationRepository = monetizedApplicationRepository;
        this.developerRepository = developerRepository;
        this.applicationRepository = applicationRepository;
        this.logger = logger;
    }

    public MonetizedApplication createMonetizedApplication(MonetizedApplicationJson monetizedApplicationJson) {
        logger.info("Creating MonetizedApplication for developer ID: " + monetizedApplicationJson.getDeveloperId() +
                ", application ID: " + monetizedApplicationJson.getApplicationId());
        Developer developer = developerRepository.findById(monetizedApplicationJson.getDeveloperId())
                .orElseThrow(() -> {
                    logger.error("Developer not found with ID: " + monetizedApplicationJson.getDeveloperId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer not found");
                });
        Application application = applicationRepository.findById(monetizedApplicationJson.getApplicationId())
                .orElseThrow(() -> {
                    logger.error("Application not found with ID: " + monetizedApplicationJson.getApplicationId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
                });
        MonetizedApplication monetizedApplication = new MonetizedApplication();
        monetizedApplication.setDeveloper(developer);
        monetizedApplication.setApplication(application);
        monetizedApplication.setCurrentBalance(monetizedApplicationJson.getCurrentBalance());
        monetizedApplication.setRevenue(monetizedApplicationJson.getRevenue());
        monetizedApplication.setDownloadRevenue(monetizedApplicationJson.getDownloadRevenue());
        monetizedApplication.setAdsRevenue(monetizedApplicationJson.getAdsRevenue());
        monetizedApplication.setPurchasesRevenue(monetizedApplicationJson.getPurchasesRevenue());
        MonetizedApplication savedMonetizedApplication = monetizedApplicationRepository.save(monetizedApplication);
        logger.info("MonetizedApplication created with ID: " + savedMonetizedApplication.getId() +
                ", developer ID: " + developer.getId() +
                ", application ID: " + application.getId());
        return savedMonetizedApplication;
    }

    public Optional<MonetizedApplication> getMonetizedApplicationById(int id) {
        logger.info("Fetching MonetizedApplication by ID: " + id);
        Optional<MonetizedApplication> monetizedApplication = monetizedApplicationRepository.findById(id);
        if (monetizedApplication.isPresent()) {
            logger.info("MonetizedApplication found with ID: " + id +
                    ", developer ID: " + monetizedApplication.get().getDeveloper().getId() +
                    ", application ID: " + monetizedApplication.get().getApplication().getId());
        } else {
            logger.info("MonetizedApplication not found with ID: " + id);
        }
        return monetizedApplication;
    }
}