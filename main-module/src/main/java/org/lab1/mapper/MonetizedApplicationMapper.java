package org.lab1.mapper;

import org.lab1.json.MonetizedApplicationJson;
import org.lab1.model.MonetizedApplication;
import org.lab1.model.Developer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MonetizedApplicationMapper {

  @Named("developerIdToDeveloper")
  default Developer developerIdToDeveloper(Integer developerId) {
    if (developerId == null || developerId == 0) {
      return null;
    }
    Developer developer = new Developer();
    developer.setId(developerId);
    return developer;
  }

  @Named("developerToDeveloperId")
  default Integer developerToDeveloperId(Developer developer) {
    return developer != null ? developer.getId() : null;
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "developerId", target = "developer", qualifiedByName = "developerIdToDeveloper")
  @Mapping(source = "applicationId", target = "application.id")
  MonetizedApplication toEntity(MonetizedApplicationJson dto);

  @Mapping(source = "developer", target = "developerId", qualifiedByName = "developerToDeveloperId")
  @Mapping(source = "application.id", target = "applicationId")
  MonetizedApplicationJson toDto(MonetizedApplication entity);
}
