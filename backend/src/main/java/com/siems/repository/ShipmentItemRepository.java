package com.siems.repository;

import com.siems.entity.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, Long> {
    List<ShipmentItem> findByShipment_ShipmentId(Long shipmentId);
}
