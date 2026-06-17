package com.siems.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class WarehouseResponse {
    private Long warehouseId;
    private String name;
    private String location;
    private String code;
    private Integer capacity;
    private Long managerId;
    private String managerName;
    private boolean active;
    private long totalItems;
    private long lowStockItems;
    private LocalDateTime createdAt;
}
