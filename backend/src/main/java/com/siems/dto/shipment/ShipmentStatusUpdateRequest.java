package com.siems.dto.shipment;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String remarks;

    private String location;
}
