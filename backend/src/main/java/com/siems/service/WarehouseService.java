package com.siems.service;

import com.siems.dto.common.PageResponse;
import com.siems.dto.warehouse.WarehouseRequest;
import com.siems.dto.warehouse.WarehouseResponse;
import org.springframework.data.domain.Pageable;

public interface WarehouseService {
    WarehouseResponse create(WarehouseRequest request);
    WarehouseResponse getById(Long id);
    WarehouseResponse update(Long id, WarehouseRequest request);
    void deactivate(Long id);
    PageResponse<WarehouseResponse> search(String keyword, boolean activeOnly, Pageable pageable);
}
