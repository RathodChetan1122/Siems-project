package com.siems.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Billing address is required")
    @Size(max = 255)
    private String billingAddress;

    @NotBlank(message = "Shipping address is required")
    @Size(max = 255)
    private String shippingAddress;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @Pattern(regexp = "^[+0-9\\-\\s]{6,20}$", message = "Phone number is invalid")
    private String phone;

    private String creditTerms;
}
