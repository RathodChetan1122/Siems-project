package com.siems.mapper;

import com.siems.dto.customer.CustomerRequest;
import com.siems.dto.customer.CustomerResponse;
import com.siems.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    Customer toEntity(CustomerRequest request);

    CustomerResponse toResponse(Customer customer);

    void updateEntityFromRequest(CustomerRequest request, @MappingTarget Customer customer);
}
