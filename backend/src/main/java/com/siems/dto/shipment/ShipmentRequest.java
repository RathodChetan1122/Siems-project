package com.siems.dto.shipment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ShipmentRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Warehouse ID is required for inventory reservation")
    private Long warehouseId;

    private String carrier;

    private LocalDate etd;

    @FutureOrPresent(message = "ETA must be today or in the future")
    private LocalDate eta;

    @NotEmpty(message = "Shipment must contain at least one item")
    @Valid
    private List<ShipmentItemRequest> items;
}
