package com.siems.repository;

import com.siems.entity.ShipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShipmentStatusHistoryRepository extends JpaRepository<ShipmentStatusHistory, Long> {
    List<ShipmentStatusHistory> findByShipment_ShipmentIdOrderByChangedAtAsc(Long shipmentId);
}
