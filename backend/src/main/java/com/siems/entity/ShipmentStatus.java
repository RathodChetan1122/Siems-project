package com.siems.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shipment_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "status_name", nullable = false, unique = true, length = 30)
    private String statusName;

    @Column(length = 255)
    private String description;
}
