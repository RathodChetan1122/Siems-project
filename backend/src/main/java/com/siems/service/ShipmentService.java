package com.siems.service;

import com.siems.dto.common.PageResponse;
import com.siems.dto.shipment.ShipmentRequest;
import com.siems.dto.shipment.ShipmentResponse;
import com.siems.dto.shipment.ShipmentStatusUpdateRequest;
import com.siems.dto.shipment.ShipmentTrackingResponse;
import org.springframework.data.domain.Pageable;

public interface ShipmentService {
    ShipmentResponse create(ShipmentRequest request);
    ShipmentResponse getById(Long id);
    ShipmentResponse updateStatus(Long id, ShipmentStatusUpdateRequest request);
    ShipmentTrackingResponse getTrackingTimeline(Long id);
    ShipmentTrackingResponse getTrackingByNumber(String trackingNumber);
    PageResponse<ShipmentResponse> search(String status, Long supplierId, Long customerId, Pageable pageable);
}
