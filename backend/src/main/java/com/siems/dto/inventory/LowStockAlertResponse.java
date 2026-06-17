package com.siems.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LowStockAlertResponse {
    private Long alertId;
    private Long inventoryId;
    private String productName;
    private String sku;
    private String warehouseName;
    private Integer currentQuantity;
    private Integer reorderThreshold;
    private LocalDateTime createdAt;
}
