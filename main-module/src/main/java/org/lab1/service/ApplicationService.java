package org.lab1.service;

import java.util.List;
import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.json.ApplicationJson;
import org.lab1.mapper.ApplicationMapper;
import org.lab1.model.Application;
import org.lab1.model.ApplicationStatus;
import org.lab1.model.ApplicationType;
import org.lab1.model.Developer;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.DeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ApplicationService {
  private static final String FETCH_ALL_APPS_LOG = "Fetching all applications";
  private static final String FOUND_APPS_LOG = "Found ";
  private static final String APPLICATIONS_LOG = " applications";
  private static final String SUBMIT_APP_LOG = "Submitting application for check: ";
  private static final String SUBMIT_SUCCESS_LOG = "Application submitted successfully with ID: ";
  private static final String SUBMIT_ERROR_LOG = "Failed to submit application: ";
  private static final String CHECK_STATUS_LOG = "Checking status for application ID: ";
  private static final String FOUND_STATUS_LOG = "Found status for application ID: ";
  private static final String STATUS_NOT_FOUND_LOG =
      "Application not found for status check with ID: ";
  private static final String FETCH_APP_LOG = "Fetching application with ID: ";
  private static final String FOUND_APP_LOG = "Found application with ID: ";
  private static final String APP_NOT_FOUND_LOG = "Application not found with ID: ";
  private static final String FETCH_BY_DEV_LOG = "Fetching applications for developer ID: ";
  private static final String FOUND_FOR_DEV_LOG = "Found ";
  private static final String APPS_FOR_DEV_LOG = " applications for developer ID: ";
  private static final String CREATE_APP_LOG = "Creating new application for developer ID: ";
  private static final String DEV_NOT_FOUND_LOG = "Developer not found with ID: ";
  private static final String DEV_NOT_FOUND_MSG = "Developer not found";
  private static final String CREATED_APP_LOG = "Created application with ID: ";
  private static final String CREATE_ERROR_LOG = "Failed to create application: ";
  private static final String LOOKUP_APP_LOG = "Looking up application with ID: ";
  private static final String APP_NOT_FOUND_MSG = "Application not found";
  private static final String UPDATE_APP_LOG = "Updating application with ID: ";
  private static final String UPDATE_SUCCESS_LOG = "Successfully updated application with ID: ";
  private static final String UPDATE_ERROR_LOG = "Failed to update application with ID: ";
  private static final String DELETE_APP_LOG = "Attempting to delete application with ID: ";
  private static final String DELETE_SUCCESS_LOG = "Successfully deleted application with ID: ";
  private static final String DELETE_NOT_FOUND_LOG = "Application not found for deletion with ID: ";
  private static final String DELETE_ERROR_LOG = "Failed to delete application with ID: ";
  private static final String ERROR_LOG = ". Error: ";

  private final ApplicationRepository applicationRepository;
  private final DeveloperRepository developerRepository;
  private final ApplicationMapper applicationMapper;
  private final Logger logger;

  @Autowired
  public ApplicationService(
      final ApplicationRepository applicationRepositoryParam,
      final DeveloperRepository developerRepositoryParam,
      final ApplicationMapper applicationMapperParam,
      final Logger loggerParam) {
    this.applicationRepository = applicationRepositoryParam;
    this.developerRepository = developerRepositoryParam;
    this.applicationMapper = applicationMapperParam;
    this.logger = loggerParam;
  }

  public final List<Application> getAllApplications() {
    logger.info(FETCH_ALL_APPS_LOG);
    List<Application> applications = applicationRepository.findAll();
    logger.info(FOUND_APPS_LOG + applications.size() + APPLICATIONS_LOG);
    return applications;
  }

  public final List<ApplicationJson> getAllApplicationsAsJson() {
    logger.info(FETCH_ALL_APPS_LOG);
    List<Application> applications = applicationRepository.findAll();
    logger.info(FOUND_APPS_LOG + applications.size() + APPLICATIONS_LOG);
    return applications.stream().map(applicationMapper::toDto).toList();
  }

  public final ResponseEntity<Application> submitApplicationForCheck(
      final Application application) {
    logger.info(SUBMIT_APP_LOG + application.getName());
    try {
      Application savedApplication = applicationRepository.save(application);
      logger.info(SUBMIT_SUCCESS_LOG + savedApplication.getId());
      return ResponseEntity.ok(savedApplication);
    } catch (Exception exception) {
      logger.error(SUBMIT_ERROR_LOG + exception.getMessage());
      throw exception;
    }
  }

  public final ResponseEntity<ApplicationJson> submitApplicationForCheckFromJson(
      final ApplicationJson applicationJson) {
    logger.info(SUBMIT_APP_LOG + applicationJson.getName());
    try {
      Application application = applicationMapper.toEntity(applicationJson);
      Application savedApplication = applicationRepository.save(application);
      logger.info(SUBMIT_SUCCESS_LOG + savedApplication.getId());
      return ResponseEntity.ok(applicationMapper.toDto(savedApplication));
    } catch (Exception exception) {
      logger.error(SUBMIT_ERROR_LOG + exception.getMessage());
      throw exception;
    }
  }

  public final ResponseEntity<ApplicationStatus> getApplicationCheckStatus(
      final int applicationId) {
    logger.info(CHECK_STATUS_LOG + applicationId);
    Optional<Application> application = applicationRepository.findById(applicationId);

    if (application.isPresent()) {
      logger.info(FOUND_STATUS_LOG + applicationId + " - " + application.get().getStatus());
      return ResponseEntity.ok(application.get().getStatus());
    }

    logger.error(STATUS_NOT_FOUND_LOG + applicationId);
    return ResponseEntity.notFound().build();
  }

  public final ResponseEntity<Application> getApplication(final int applicationId) {
    logger.info(FETCH_APP_LOG + applicationId);
    Optional<Application> application = applicationRepository.findById(applicationId);

    if (application.isPresent()) {
      logger.info(FOUND_APP_LOG + applicationId);
      return ResponseEntity.ok(application.get());
    }

    logger.error(APP_NOT_FOUND_LOG + applicationId);
    return ResponseEntity.notFound().build();
  }

  public final ResponseEntity<ApplicationJson> getApplicationAsJson(final int applicationId) {
    logger.info(FETCH_APP_LOG + applicationId);
    Optional<Application> application = applicationRepository.findById(applicationId);

    if (application.isPresent()) {
      logger.info(FOUND_APP_LOG + applicationId);
      return ResponseEntity.ok(applicationMapper.toDto(application.get()));
    }

    logger.error(APP_NOT_FOUND_LOG + applicationId);
    return ResponseEntity.notFound().build();
  }

  public final List<Application> getApplicationsByDeveloperId(final int developerId) {
    logger.info(FETCH_BY_DEV_LOG + developerId);
    List<Application> applications = applicationRepository.findByDeveloperId(developerId);
    logger.info(FOUND_FOR_DEV_LOG + applications.size() + APPS_FOR_DEV_LOG + developerId);
    return applications;
  }

  public final List<ApplicationJson> getApplicationsByDeveloperIdAsJson(final int developerId) {
    logger.info(FETCH_BY_DEV_LOG + developerId);
    List<Application> applications = applicationRepository.findByDeveloperId(developerId);
    logger.info(FOUND_FOR_DEV_LOG + applications.size() + APPS_FOR_DEV_LOG + developerId);
    return applications.stream().map(applicationMapper::toDto).toList();
  }

  public final Application createApplication(final ApplicationJson applicationJson) {
    logger.info(CREATE_APP_LOG + applicationJson.getDeveloperId());

    Developer developer =
        developerRepository
            .findById(applicationJson.getDeveloperId())
            .orElseThrow(
                () -> {
                  logger.error(DEV_NOT_FOUND_LOG + applicationJson.getDeveloperId());
                  return new ResponseStatusException(HttpStatus.NOT_FOUND, DEV_NOT_FOUND_MSG);
                });

    Application application = applicationMapper.toEntity(applicationJson);
    application.setDeveloper(developer);

    try {
      Application savedApplication = applicationRepository.save(application);
      logger.info(CREATED_APP_LOG + savedApplication.getId());
      return savedApplication;
    } catch (Exception exception) {
      logger.error(CREATE_ERROR_LOG + exception.getMessage());
      throw exception;
    }
  }

  public final ApplicationJson createApplicationAndReturnJson(
      final ApplicationJson applicationJson) {
    Application application = createApplication(applicationJson);
    return applicationMapper.toDto(application);
  }

  public final Optional<Application> getApplicationById(final int id) {
    logger.info(LOOKUP_APP_LOG + id);
    Optional<Application> application = applicationRepository.findById(id);

    if (application.isPresent()) {
      logger.info(FOUND_APP_LOG + id);
    } else {
      logger.error(APP_NOT_FOUND_LOG + id);
    }

    return application;
  }

  public final Optional<ApplicationJson> getApplicationJsonById(final int id) {
    Optional<Application> application = getApplicationById(id);
    return application.map(applicationMapper::toDto);
  }

  public final Application updateApplication(
      final int id,
      final Developer developer,
      final String name,
      final ApplicationType type,
      final double price,
      final String description,
      final ApplicationStatus status) {
    logger.info(UPDATE_APP_LOG + id);
    Application application =
        applicationRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  logger.error(APP_NOT_FOUND_LOG + id);
                  return new ResponseStatusException(HttpStatus.NOT_FOUND, APP_NOT_FOUND_MSG);
                });

    application.setDeveloper(developer);
    application.setName(name);
    application.setType(type);
    application.setPrice(price);
    application.setDescription(description);
    application.setStatus(status);

    try {
      Application updatedApplication = applicationRepository.save(application);
      logger.info(UPDATE_SUCCESS_LOG + id);
      return updatedApplication;
    } catch (Exception exception) {
      logger.error(UPDATE_ERROR_LOG + id + ERROR_LOG + exception.getMessage());
      throw exception;
    }
  }

  public final ApplicationJson updateApplicationFromJson(
      final int id, final ApplicationJson applicationJson) {
    logger.info(UPDATE_APP_LOG + id);

    Application application =
        applicationRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  logger.error(APP_NOT_FOUND_LOG + id);
                  return new ResponseStatusException(HttpStatus.NOT_FOUND, APP_NOT_FOUND_MSG);
                });

    if (applicationJson.getDeveloperId() > 0) {
      Developer developer =
          developerRepository
              .findById(applicationJson.getDeveloperId())
              .orElseThrow(
                  () -> {
                    logger.error(DEV_NOT_FOUND_LOG + applicationJson.getDeveloperId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, DEV_NOT_FOUND_MSG);
                  });
      application.setDeveloper(developer);
    }

    applicationMapper.updateEntityFromDto(applicationJson, application);

    try {
      Application updatedApplication = applicationRepository.save(application);
      logger.info(UPDATE_SUCCESS_LOG + id);
      return applicationMapper.toDto(updatedApplication);
    } catch (Exception exception) {
      logger.error(UPDATE_ERROR_LOG + id + ERROR_LOG + exception.getMessage());
      throw exception;
    }
  }

  public final void deleteApplication(final int id) {
    logger.info(DELETE_APP_LOG + id);
    try {
      if (applicationRepository.existsById(id)) {
        applicationRepository.deleteById(id);
        logger.info(DELETE_SUCCESS_LOG + id);
      } else {
        logger.error(DELETE_NOT_FOUND_LOG + id);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, APP_NOT_FOUND_MSG);
      }
    } catch (Exception exception) {
      logger.error(DELETE_ERROR_LOG + id + ERROR_LOG + exception.getMessage());
      throw exception;
    }
  }
}
