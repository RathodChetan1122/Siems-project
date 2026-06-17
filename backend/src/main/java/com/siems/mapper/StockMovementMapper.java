package com.siems.mapper;

import com.siems.dto.inventory.StockMovementResponse;
import com.siems.entity.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    @Mapping(target = "productName", source = "inventory.product.name")
    @Mapping(target = "sku", source = "inventory.product.sku")
    @Mapping(target = "warehouseName", source = "inventory.warehouse.name")
    @Mapping(target = "movementType", expression = "java(movement.getMovementType().name())")
    @Mapping(target = "performedBy", source = "performedBy.username")
    StockMovementResponse toResponse(StockMovement movement);
}
