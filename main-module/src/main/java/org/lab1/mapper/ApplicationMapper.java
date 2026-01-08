package org.lab1.mapper;

import org.lab1.json.ApplicationJson;
import org.lab1.model.Application;
import org.lab1.model.Developer;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

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
  Application toEntity(ApplicationJson dto);

  @Mapping(source = "developer", target = "developerId", qualifiedByName = "developerToDeveloperId")
  ApplicationJson toDto(Application entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(source = "developerId", target = "developer", qualifiedByName = "developerIdToDeveloper")
  void updateEntityFromDto(ApplicationJson dto, @MappingTarget Application entity);
}
