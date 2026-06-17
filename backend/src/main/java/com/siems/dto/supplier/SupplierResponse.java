package com.siems.dto.supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SupplierResponse {
    private Long supplierId;
    private String name;
    private String country;
    private String contactEmail;
    private String phone;
    private BigDecimal rating;
    private String address;
    private LocalDateTime createdAt;
}
