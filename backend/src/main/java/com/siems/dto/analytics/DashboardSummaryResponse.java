package com.siems.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalShipments;
    private long shipmentsInTransit;
    private long shipmentsDelivered;
    private long lowStockItemsCount;
    private long totalSuppliers;
    private long totalCustomers;
    private long totalProducts;
    private BigDecimal totalStockValue;
    private Map<String, Long> shipmentsByStatus;
}
