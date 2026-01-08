package org.lab1.mapper;

import org.lab1.json.InAppPurchaseJson;
import org.lab1.model.InAppPurchase;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InAppPurchaseMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "monetizedApplicationId", target = "monetizedApplication.id")
  InAppPurchase toEntity(InAppPurchaseJson dto);

  @Mapping(source = "monetizedApplication.id", target = "monetizedApplicationId")
  InAppPurchaseJson toDto(InAppPurchase entity);
}
