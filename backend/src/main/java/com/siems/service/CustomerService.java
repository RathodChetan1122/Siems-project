package com.siems.service;

import com.siems.dto.common.PageResponse;
import com.siems.dto.customer.CustomerRequest;
import com.siems.dto.customer.CustomerResponse;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerResponse create(CustomerRequest request);
    CustomerResponse getById(Long id);
    CustomerResponse update(Long id, CustomerRequest request);
    void delete(Long id);
    PageResponse<CustomerResponse> search(String keyword, Pageable pageable);
}
