package org.lab1.service;

import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.json.MonetizedApplicationJson;
import org.lab1.mapper.MonetizedApplicationMapper;
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
  private final MonetizedApplicationMapper monetizedApplicationMapper;
  private final Logger logger;

  @Autowired
  public MonetizedApplicationService(
      final MonetizedApplicationRepository monetizedApplicationRepositoryParam,
      final DeveloperRepository developerRepositoryParam,
      final ApplicationRepository applicationRepositoryParam,
      final MonetizedApplicationMapper monetizedApplicationMapperParam,
      final Logger loggerParam) {
    this.monetizedApplicationRepository = monetizedApplicationRepositoryParam;
    this.developerRepository = developerRepositoryParam;
    this.applicationRepository = applicationRepositoryParam;
    this.monetizedApplicationMapper = monetizedApplicationMapperParam;
    this.logger = loggerParam;
  }

  public final MonetizedApplication createMonetizedApplication(
      final MonetizedApplicationJson monetizedApplicationJson) {
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

    MonetizedApplication monetizedApplication =
        monetizedApplicationMapper.toEntity(monetizedApplicationJson);
    monetizedApplication.setDeveloper(developer);
    monetizedApplication.setApplication(application);

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

  public final MonetizedApplicationJson createMonetizedApplicationAndReturnJson(
      final MonetizedApplicationJson monetizedApplicationJson) {
    MonetizedApplication savedMonetizedApplication =
        createMonetizedApplication(monetizedApplicationJson);
    return monetizedApplicationMapper.toDto(savedMonetizedApplication);
  }

  public final Optional<MonetizedApplication> getMonetizedApplicationById(final int id) {
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

  public final Optional<MonetizedApplicationJson> getMonetizedApplicationByIdAsJson(final int id) {
    Optional<MonetizedApplication> monetizedApplication = getMonetizedApplicationById(id);
    return monetizedApplication.map(monetizedApplicationMapper::toDto);
  }

  public final MonetizedApplication getMonetizedApplicationByApplicationId(
      final int applicationId) {
    MonetizedApplication monetizedApp =
        monetizedApplicationRepository.findByApplicationId(applicationId);

    if (monetizedApp == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Monetized application not found");
    }

    return monetizedApp;
  }
}
