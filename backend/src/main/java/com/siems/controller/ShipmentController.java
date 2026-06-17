package com.siems.controller;

import com.siems.dto.common.ApiResponse;
import com.siems.dto.common.PageResponse;
import com.siems.dto.shipment.*;
import com.siems.service.ShipmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
@Tag(name = "Shipments", description = "Shipment creation, tracking, and status updates")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','IMPORT_MANAGER','EXPORT_MANAGER')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> create(@Valid @RequestBody ShipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shipment created successfully", shipmentService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(shipmentService.getById(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','IMPORT_MANAGER','EXPORT_MANAGER','INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody ShipmentStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Shipment status updated successfully",
                shipmentService.updateStatus(id, request)));
    }

    @GetMapping("/{id}/tracking")
    public ResponseEntity<ApiResponse<ShipmentTrackingResponse>> getTracking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(shipmentService.getTrackingTimeline(id)));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ApiResponse<ShipmentTrackingResponse>> trackByNumber(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(ApiResponse.success(shipmentService.getTrackingByNumber(trackingNumber)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ShipmentResponse>>> search(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long customerId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(shipmentService.search(status, supplierId, customerId, pageable)));
    }
}
