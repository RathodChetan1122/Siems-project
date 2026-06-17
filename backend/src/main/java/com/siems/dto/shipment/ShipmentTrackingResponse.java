package com.siems.dto.shipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ShipmentTrackingResponse {
    private Long shipmentId;
    private String trackingNumber;
    private String currentStatus;
    private String carrier;
    private LocalDate etd;
    private LocalDate eta;
    private List<TrackingEvent> timeline;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TrackingEvent {
        private String status;
        private String location;
        private String remarks;
        private String changedBy;
        private LocalDateTime changedAt;
    }
}
