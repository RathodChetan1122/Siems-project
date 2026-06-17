package com.siems.mapper;

import com.siems.dto.product.ProductRequest;
import com.siems.dto.product.ProductResponse;
import com.siems.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "supplier", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "supplierId", source = "supplier.supplierId")
    @Mapping(target = "supplierName", source = "supplier.name")
    ProductResponse toResponse(Product product);

    @Mapping(target = "supplier", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);
}
