package com.siems.service.impl;

import com.siems.dto.analytics.DashboardSummaryResponse;
import com.siems.repository.*;
import com.siems.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ShipmentRepository shipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    private static final List<String> ALL_STATUSES =
            List.of("PENDING", "PACKED", "DISPATCHED", "IN_TRANSIT", "AT_CUSTOMS", "DELIVERED", "CANCELLED");

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        Map<String, Long> shipmentsByStatus = new LinkedHashMap<>();
        for (String status : ALL_STATUSES) {
            shipmentsByStatus.put(status, shipmentRepository.countByCurrentStatus_StatusName(status));
        }

        long totalShipments = shipmentsByStatus.values().stream().mapToLong(Long::longValue).sum();

        return DashboardSummaryResponse.builder()
                .totalShipments(totalShipments)
                .shipmentsInTransit(shipmentsByStatus.getOrDefault("IN_TRANSIT", 0L))
                .shipmentsDelivered(shipmentsByStatus.getOrDefault("DELIVERED", 0L))
                .lowStockItemsCount(inventoryRepository.findLowStockItems().size())
                .totalSuppliers(supplierRepository.count())
                .totalCustomers(customerRepository.count())
                .totalProducts(productRepository.count())
                .totalStockValue(inventoryRepository.sumTotalStockValue())
                .shipmentsByStatus(shipmentsByStatus)
                .build();
    }
}
