package com.siems.repository;

import com.siems.entity.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    boolean existsByTrackingNumber(String trackingNumber);

    @Query("""
           SELECT s FROM Shipment s
           WHERE (cast(:status as string) IS NULL OR s.currentStatus.statusName = :status)
             AND (cast(:supplierId as long) IS NULL OR s.supplier.supplierId = :supplierId)
             AND (cast(:customerId as long) IS NULL OR s.customer.customerId = :customerId)
           """)
    Page<Shipment> search(@Param("status") String status,
                           @Param("supplierId") Long supplierId,
                           @Param("customerId") Long customerId,
                           Pageable pageable);

    long countByCurrentStatus_StatusName(String statusName);
}
