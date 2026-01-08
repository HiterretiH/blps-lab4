package org.lab1.mapper;

import org.lab1.json.InAppAddJson;
import org.lab1.model.InAppAdd;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InAppAddMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "monetizedApplicationId", target = "monetizedApplication.id")
  InAppAdd toEntity(InAppAddJson dto);

  @Mapping(source = "monetizedApplication.id", target = "monetizedApplicationId")
  InAppAddJson toDto(InAppAdd entity);
}
