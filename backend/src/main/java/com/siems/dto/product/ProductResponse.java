package com.siems.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {
    private Long productId;
    private String sku;
    private String name;
    private String category;
    private String unitOfMeasure;
    private BigDecimal unitPrice;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime createdAt;
}
