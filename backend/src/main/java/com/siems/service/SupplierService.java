package com.siems.service;

import com.siems.dto.common.PageResponse;
import com.siems.dto.supplier.SupplierRequest;
import com.siems.dto.supplier.SupplierResponse;
import org.springframework.data.domain.Pageable;

public interface SupplierService {
    SupplierResponse create(SupplierRequest request);
    SupplierResponse getById(Long id);
    SupplierResponse update(Long id, SupplierRequest request);
    void delete(Long id);
    PageResponse<SupplierResponse> search(String name, String country, Pageable pageable);
}
