package com.siems.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "billing_address", nullable = false, length = 255)
    private String billingAddress;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @Column(name = "contact_email", nullable = false, unique = true, length = 100)
    private String contactEmail;

    @Column(length = 20)
    private String phone;

    @Column(name = "credit_terms", length = 50)
    @Builder.Default
    private String creditTerms = "NET_30";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
