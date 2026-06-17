package com.siems.mapper;

import com.siems.dto.inventory.InventoryResponse;
import com.siems.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "warehouseId", source = "warehouse.warehouseId")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "lowStock", expression = "java(inventory.isLowStock())")
    InventoryResponse toResponse(Inventory inventory);
}
