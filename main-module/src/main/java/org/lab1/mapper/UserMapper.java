package org.lab1.mapper;

import org.lab1.json.UserJson;
import org.lab1.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserJson toDto(User entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  User toEntity(UserJson dto);
}
