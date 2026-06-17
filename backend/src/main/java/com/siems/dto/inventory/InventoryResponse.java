package com.siems.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class InventoryResponse {
    private Long inventoryId;
    private Long productId;
    private String productName;
    private String sku;
    private Long warehouseId;
    private String warehouseName;
    private Integer quantity;
    private Integer reorderThreshold;
    private boolean lowStock;
    private LocalDateTime lastUpdated;
}
