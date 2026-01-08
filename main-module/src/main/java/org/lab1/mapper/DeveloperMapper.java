package org.lab1.mapper;

import org.lab1.json.DeveloperJson;
import org.lab1.model.Developer;
import org.lab1.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DeveloperMapper {

  @Named("userIdToUser")
  default User userIdToUser(Integer userId) {
    if (userId == null || userId == 0) {
      return null;
    }
    User user = new User();
    user.setId(userId);
    return user;
  }

  @Named("userToUserId")
  default Integer userToUserId(User user) {
    return user != null ? user.getId() : null;
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "userId", target = "user", qualifiedByName = "userIdToUser")
  Developer toEntity(DeveloperJson dto);

  @Mapping(source = "user", target = "userId", qualifiedByName = "userToUserId")
  DeveloperJson toDto(Developer entity);
}
