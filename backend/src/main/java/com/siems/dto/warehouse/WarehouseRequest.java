package com.siems.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseRequest {

    @NotBlank(message = "Warehouse name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Warehouse code is required")
    private String code;

    @Min(value = 1, message = "Capacity must be positive")
    private Integer capacity;

    private Long managerId;
}
