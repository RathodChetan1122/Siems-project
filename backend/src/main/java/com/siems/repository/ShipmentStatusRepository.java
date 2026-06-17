package com.siems.repository;

import com.siems.entity.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShipmentStatusRepository extends JpaRepository<ShipmentStatus, Long> {
    Optional<ShipmentStatus> findByStatusName(String statusName);
}
