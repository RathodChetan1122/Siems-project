package com.siems.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StockMovementResponse {
    private Long movementId;
    private String productName;
    private String sku;
    private String warehouseName;
    private String movementType;
    private Integer quantity;
    private String referenceType;
    private Long referenceId;
    private String reason;
    private String performedBy;
    private LocalDateTime createdAt;
}
