package com.siems.mapper;

import com.siems.dto.shipment.ShipmentResponse;
import com.siems.entity.Shipment;
import com.siems.entity.ShipmentItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    @Mapping(target = "supplierId", source = "supplier.supplierId")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "currentStatus", source = "currentStatus.statusName")
    @Mapping(target = "items", source = "items")
    ShipmentResponse toResponse(Shipment shipment);

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    ShipmentResponse.ItemDto toItemDto(ShipmentItem item);

    List<ShipmentResponse.ItemDto> toItemDtoList(List<ShipmentItem> items);
}
