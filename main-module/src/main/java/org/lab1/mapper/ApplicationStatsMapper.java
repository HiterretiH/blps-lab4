package org.lab1.mapper;

import org.lab1.json.ApplicationStatsJson;
import org.lab1.model.ApplicationStats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationStatsMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "applicationId", target = "application.id")
  ApplicationStats toEntity(ApplicationStatsJson dto);

  @Mapping(source = "application.id", target = "applicationId")
  ApplicationStatsJson toDto(ApplicationStats entity);
}
