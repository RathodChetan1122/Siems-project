package com.siems.dto.shipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ShipmentResponse {
    private Long shipmentId;
    private String trackingNumber;
    private Long supplierId;
    private String supplierName;
    private Long customerId;
    private String customerName;
    private String currentStatus;
    private String carrier;
    private LocalDate etd;
    private LocalDate eta;
    private List<ItemDto> items;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ItemDto {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
