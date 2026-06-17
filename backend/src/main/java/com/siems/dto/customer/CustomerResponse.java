package com.siems.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CustomerResponse {
    private Long customerId;
    private String name;
    private String billingAddress;
    private String shippingAddress;
    private String contactEmail;
    private String phone;
    private String creditTerms;
    private LocalDateTime createdAt;
}
