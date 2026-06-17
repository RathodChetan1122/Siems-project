package com.siems.dto.supplier;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Country is required")
    @Size(max = 80)
    private String country;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @Pattern(regexp = "^[+0-9\\-\\s]{6,20}$", message = "Phone number is invalid")
    private String phone;

    @DecimalMin(value = "0.0", message = "Rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5")
    private BigDecimal rating;

    @Size(max = 255)
    private String address;
}
