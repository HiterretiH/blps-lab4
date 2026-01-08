package org.lab1.service;

import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.exception.NotFoundException;
import org.lab1.json.DeveloperJson;
import org.lab1.mapper.DeveloperMapper;
import org.lab1.model.Developer;
import org.lab1.model.User;
import org.lab1.repository.DeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeveloperService {
  private static final String CREATE_DEV_LOG = "Creating developer with name: ";
  private static final String CREATED_DEV_LOG = "Developer created with ID: ";
  private static final String CREATE_FROM_USER_LOG = "Creating developer from user: ";
  private static final String CREATED_FROM_USER_LOG = "Developer created with ID: ";
  private static final String FROM_USER_LOG = " from user: ";
  private static final String FETCH_DEV_LOG = "Fetching developer by ID: ";
  private static final String DEV_FOUND_LOG = "Developer found with ID: ";
  private static final String DEV_NOT_FOUND_LOG = "Developer not found with ID: ";
  private static final String UPDATE_DEV_LOG = "Updating developer with ID: ";
  private static final String NAME_LOG = ", name: ";
  private static final String UPDATE_NOT_FOUND_LOG = "Developer not found with ID: ";
  private static final String UPDATE_NOT_FOUND_MSG = " for update.";
  private static final String DEV_NOT_FOUND_MSG = "Developer not found";
  private static final String UPDATED_DEV_LOG = "Developer updated with ID: ";
  private static final String DELETE_DEV_LOG = "Deleting developer with ID: ";
  private static final String DELETED_DEV_LOG = "Developer deleted with ID: ";

  private final DeveloperRepository developerRepository;
  private final DeveloperMapper developerMapper;
  private final Logger logger;

  @Autowired
  public DeveloperService(
      final DeveloperRepository developerRepositoryParam,
      final DeveloperMapper developerMapperParam,
      final Logger loggerParam) {
    this.developerRepository = developerRepositoryParam;
    this.developerMapper = developerMapperParam;
    this.logger = loggerParam;
  }

  public final Developer createDeveloper(final String name, final String description) {
    logger.info(CREATE_DEV_LOG + name);
    Developer developer = new Developer();
    developer.setName(name);
    developer.setDescription(description);
    Developer savedDeveloper = developerRepository.save(developer);
    logger.info(CREATED_DEV_LOG + savedDeveloper.getId());
    return savedDeveloper;
  }

  public final DeveloperJson createDeveloperFromJson(final DeveloperJson developerJson) {
    logger.info(CREATE_DEV_LOG + developerJson.getName());
    Developer developer = developerMapper.toEntity(developerJson);
    Developer savedDeveloper = developerRepository.save(developer);
    logger.info(CREATED_DEV_LOG + savedDeveloper.getId());
    return developerMapper.toDto(savedDeveloper);
  }

  public final Developer createDeveloper(final User user) {
    logger.info(CREATE_FROM_USER_LOG + user.getUsername());
    Developer developer = new Developer();
    developer.setName(user.getUsername());
    developer.setDescription(user.getEmail());
    developer.setUser(user);
    Developer savedDeveloper = developerRepository.save(developer);
    logger.info(
        CREATED_FROM_USER_LOG + savedDeveloper.getId() + FROM_USER_LOG + user.getUsername());
    return savedDeveloper;
  }

  public final Optional<Developer> getDeveloperById(final int id) {
    logger.info(FETCH_DEV_LOG + id);
    Optional<Developer> developer = developerRepository.findById(id);

    if (developer.isPresent()) {
      logger.info(DEV_FOUND_LOG + id);
    } else {
      logger.info(DEV_NOT_FOUND_LOG + id);
    }

    return developer;
  }

  public final Optional<DeveloperJson> getDeveloperByIdAsJson(final int id) {
    Optional<Developer> developer = getDeveloperById(id);
    return developer.map(developerMapper::toDto);
  }

  public final Developer updateDeveloper(
      final int id, final String name, final String description) {
    logger.info(UPDATE_DEV_LOG + id + NAME_LOG + name);
    Developer developer =
        developerRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  logger.error(UPDATE_NOT_FOUND_LOG + id + UPDATE_NOT_FOUND_MSG);
                  return new NotFoundException("Developer not found with ID: " + id);
                });
    developer.setName(name);
    developer.setDescription(description);
    Developer updatedDeveloper = developerRepository.save(developer);
    logger.info(UPDATED_DEV_LOG + updatedDeveloper.getId());
    return updatedDeveloper;
  }

  public final DeveloperJson updateDeveloperFromJson(
      final int id, final DeveloperJson developerJson) {
    logger.info(UPDATE_DEV_LOG + id + NAME_LOG + developerJson.getName());

    Developer developer =
        developerRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  logger.error(UPDATE_NOT_FOUND_LOG + id + UPDATE_NOT_FOUND_MSG);
                  return new NotFoundException("Developer not found with ID: " + id);
                });

    developer.setName(developerJson.getName());
    developer.setDescription(developerJson.getDescription());

    Developer updatedDeveloper = developerRepository.save(developer);
    logger.info(UPDATED_DEV_LOG + updatedDeveloper.getId());
    return developerMapper.toDto(updatedDeveloper);
  }

  public final void deleteDeveloper(final int id) {
    logger.info(DELETE_DEV_LOG + id);
    developerRepository.deleteById(id);
    logger.info(DELETED_DEV_LOG + id);
  }
}
