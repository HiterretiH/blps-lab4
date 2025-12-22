package org.lab1.service;

import java.util.Optional;
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

@Service
public class MonetizedApplicationService {
  private static final String CREATE_REQUEST_LOG =
      "Creating MonetizedApplication for developer ID: ";
  private static final String APPLICATION_ID_LOG = ", application ID: ";
  private static final String DEV_NOT_FOUND_LOG = "Developer not found with ID: ";
  private static final String DEV_NOT_FOUND_MSG = "Developer not found";
  private static final String APP_NOT_FOUND_LOG = "Application not found with ID: ";
  private static final String APP_NOT_FOUND_MSG = "Application not found";
  private static final String CREATED_LOG = "MonetizedApplication created with ID: ";
  private static final String DEVELOPER_ID_LOG = ", developer ID: ";
  private static final String FETCH_BY_ID_LOG = "Fetching MonetizedApplication by ID: ";
  private static final String FOUND_BY_ID_LOG = "MonetizedApplication found with ID: ";
  private static final String NOT_FOUND_BY_ID_LOG = "MonetizedApplication not found with ID: ";

  private final MonetizedApplicationRepository monetizedApplicationRepository;
  private final DeveloperRepository developerRepository;
  private final ApplicationRepository applicationRepository;
  private final Logger logger;

  @Autowired
  public MonetizedApplicationService(
      MonetizedApplicationRepository monetizedApplicationRepository,
      DeveloperRepository developerRepository,
      ApplicationRepository applicationRepository,
      Logger logger) {
    this.monetizedApplicationRepository = monetizedApplicationRepository;
    this.developerRepository = developerRepository;
    this.applicationRepository = applicationRepository;
    this.logger = logger;
  }

  public MonetizedApplication createMonetizedApplication(
      MonetizedApplicationJson monetizedApplicationJson) {
    logger.info(
        CREATE_REQUEST_LOG
            + monetizedApplicationJson.getDeveloperId()
            + APPLICATION_ID_LOG
            + monetizedApplicationJson.getApplicationId());

    Developer developer =
        developerRepository
            .findById(monetizedApplicationJson.getDeveloperId())
            .orElseThrow(
                () -> {
                  logger.error(DEV_NOT_FOUND_LOG + monetizedApplicationJson.getDeveloperId());
                  return new ResponseStatusException(HttpStatus.NOT_FOUND, DEV_NOT_FOUND_MSG);
                });

    Application application =
        applicationRepository
            .findById(monetizedApplicationJson.getApplicationId())
            .orElseThrow(
                () -> {
                  logger.error(APP_NOT_FOUND_LOG + monetizedApplicationJson.getApplicationId());
                  return new ResponseStatusException(HttpStatus.NOT_FOUND, APP_NOT_FOUND_MSG);
                });

    MonetizedApplication monetizedApplication = new MonetizedApplication();
    monetizedApplication.setDeveloper(developer);
    monetizedApplication.setApplication(application);
    monetizedApplication.setCurrentBalance(monetizedApplicationJson.getCurrentBalance());
    monetizedApplication.setRevenue(monetizedApplicationJson.getRevenue());
    monetizedApplication.setDownloadRevenue(monetizedApplicationJson.getDownloadRevenue());
    monetizedApplication.setAdsRevenue(monetizedApplicationJson.getAdsRevenue());
    monetizedApplication.setPurchasesRevenue(monetizedApplicationJson.getPurchasesRevenue());

    MonetizedApplication savedMonetizedApplication =
        monetizedApplicationRepository.save(monetizedApplication);
    logger.info(
        CREATED_LOG
            + savedMonetizedApplication.getId()
            + DEVELOPER_ID_LOG
            + developer.getId()
            + APPLICATION_ID_LOG
            + application.getId());
    return savedMonetizedApplication;
  }

  public Optional<MonetizedApplication> getMonetizedApplicationById(int id) {
    logger.info(FETCH_BY_ID_LOG + id);
    Optional<MonetizedApplication> monetizedApplication =
        monetizedApplicationRepository.findById(id);

    if (monetizedApplication.isPresent()) {
      logger.info(
          FOUND_BY_ID_LOG
              + id
              + DEVELOPER_ID_LOG
              + monetizedApplication.get().getDeveloper().getId()
              + APPLICATION_ID_LOG
              + monetizedApplication.get().getApplication().getId());
    } else {
      logger.info(NOT_FOUND_BY_ID_LOG + id);
    }

    return monetizedApplication;
  }
}
