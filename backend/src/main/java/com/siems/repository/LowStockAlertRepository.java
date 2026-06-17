package com.siems.repository;

import com.siems.entity.LowStockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LowStockAlertRepository extends JpaRepository<LowStockAlert, Long> {

    Optional<LowStockAlert> findByInventory_InventoryIdAndResolvedFalse(Long inventoryId);

    @Query("SELECT a FROM LowStockAlert a WHERE a.resolved = false ORDER BY a.createdAt DESC")
    List<LowStockAlert> findAllUnresolved();
}
