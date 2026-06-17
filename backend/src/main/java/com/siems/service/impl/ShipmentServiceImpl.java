package com.siems.service.impl;

import com.siems.dto.common.PageResponse;
import com.siems.dto.shipment.*;
import com.siems.entity.*;
import com.siems.entity.enums.ShipmentStatusEnum;
import com.siems.exception.BadRequestException;
import com.siems.exception.ResourceNotFoundException;
import com.siems.mapper.ShipmentMapper;
import com.siems.repository.*;
import com.siems.security.SecurityUtils;
import com.siems.service.InventoryService;
import com.siems.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final ShipmentStatusRepository shipmentStatusRepository;
    private final ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;
    private final InventoryService inventoryService;
    private final ShipmentMapper shipmentMapper;

    @Override
    public ShipmentResponse create(ShipmentRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> ResourceNotFoundException.of("Supplier", request.getSupplierId()));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", request.getCustomerId()));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> ResourceNotFoundException.of("Warehouse", request.getWarehouseId()));

        ShipmentStatus pendingStatus = shipmentStatusRepository.findByStatusName(ShipmentStatusEnum.PENDING.name())
                .orElseThrow(() -> new ResourceNotFoundException("Status PENDING not configured"));

        Shipment shipment = Shipment.builder()
                .trackingNumber(generateTrackingNumber())
                .supplier(supplier)
                .customer(customer)
                .warehouse(warehouse)
                .createdBy(currentUserOrNull())
                .currentStatus(pendingStatus)
                .carrier(request.getCarrier())
                .etd(request.getEtd())
                .eta(request.getEta())
                .build();

        for (ShipmentItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Product", itemReq.getProductId()));

            // Reserve inventory immediately (PESSIMISTIC_WRITE lock inside InventoryService)
            inventoryService.reserveStock(itemReq.getProductId(), request.getWarehouseId(), itemReq.getQuantity());

            shipment.addItem(ShipmentItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .build());
        }

        Shipment saved = shipmentRepository.save(shipment);

        recordHistory(saved, pendingStatus, "Shipment created — inventory reserved", "Warehouse");

        return shipmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getById(Long id) {
        return shipmentMapper.toResponse(findShipmentOrThrow(id));
    }

    @Override
    public ShipmentResponse updateStatus(Long id, ShipmentStatusUpdateRequest request) {
        Shipment shipment = findShipmentOrThrow(id);

        ShipmentStatusEnum currentStatusEnum = ShipmentStatusEnum.valueOf(shipment.getCurrentStatus().getStatusName());
        ShipmentStatusEnum targetStatusEnum;

        try {
            targetStatusEnum = ShipmentStatusEnum.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid shipment status: " + request.getStatus());
        }

        validateTransition(currentStatusEnum, targetStatusEnum);

        ShipmentStatus newStatus = shipmentStatusRepository.findByStatusName(targetStatusEnum.name())
                .orElseThrow(() -> new ResourceNotFoundException("Status not configured: " + targetStatusEnum));

        if (targetStatusEnum == ShipmentStatusEnum.CANCELLED && currentStatusEnum.restoresInventoryOnCancel()) {
            restoreShipmentInventory(shipment);
        }

        shipment.changeStatus(newStatus);
        shipment.setUpdatedAt(LocalDateTime.now());
        Shipment updated = shipmentRepository.save(shipment);

        recordHistory(updated, newStatus, request.getRemarks(), request.getLocation());

        return shipmentMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentTrackingResponse getTrackingTimeline(Long id) {
        return buildTrackingResponse(findShipmentOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentTrackingResponse getTrackingByNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with tracking number: " + trackingNumber));
        return buildTrackingResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShipmentResponse> search(String status, Long supplierId, Long customerId, Pageable pageable) {
        Page<Shipment> page = shipmentRepository.search(status, supplierId, customerId, pageable);
        return PageResponse.from(page.map(shipmentMapper::toResponse));
    }

    // ---------------- PRIVATE HELPERS ----------------

    private void validateTransition(ShipmentStatusEnum current, ShipmentStatusEnum target) {
        if (current.isTerminal()) {
            throw new BadRequestException(
                    "Cannot change status: shipment is already in terminal state " + current);
        }
        if (current == target) {
            throw new BadRequestException("Shipment is already in status: " + current);
        }
        if (!current.canTransitionTo(target)) {
            throw new BadRequestException(
                    "Invalid status transition: " + current + " -> " + target
                            + ". Allowed: " + current.allowedNextStatuses());
        }
    }

    private void restoreShipmentInventory(Shipment shipment) {
        Long warehouseId = shipment.getWarehouse() != null ? shipment.getWarehouse().getWarehouseId() : null;
        if (warehouseId == null) return;

        for (ShipmentItem item : shipment.getItems()) {
            inventoryService.restoreStock(item.getProduct().getProductId(), warehouseId, item.getQuantity());
        }
    }

    private void recordHistory(Shipment shipment, ShipmentStatus status, String remarks, String location) {
        ShipmentStatusHistory history = ShipmentStatusHistory.builder()
                .shipment(shipment)
                .status(status)
                .changedBy(currentUserOrNull())
                .remarks(remarks)
                .location(location)
                .build();
        shipmentStatusHistoryRepository.save(history);
    }

    private ShipmentTrackingResponse buildTrackingResponse(Shipment shipment) {
        List<ShipmentStatusHistory> history =
                shipmentStatusHistoryRepository.findByShipment_ShipmentIdOrderByChangedAtAsc(shipment.getShipmentId());

        List<ShipmentTrackingResponse.TrackingEvent> timeline = history.stream()
                .map(h -> ShipmentTrackingResponse.TrackingEvent.builder()
                        .status(h.getStatus().getStatusName())
                        .location(h.getLocation())
                        .remarks(h.getRemarks())
                        .changedBy(h.getChangedBy() != null ? h.getChangedBy().getUsername() : "SYSTEM")
                        .changedAt(h.getChangedAt())
                        .build())
                .toList();

        return ShipmentTrackingResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .trackingNumber(shipment.getTrackingNumber())
                .currentStatus(shipment.getCurrentStatus().getStatusName())
                .carrier(shipment.getCarrier())
                .etd(shipment.getEtd())
                .eta(shipment.getEta())
                .timeline(timeline)
                .build();
    }

    private Shipment findShipmentOrThrow(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Shipment", id));
    }

    private String generateTrackingNumber() {
        return "SIEMS-SHP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private User currentUserOrNull() {
        try {
            return SecurityUtils.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }
}
