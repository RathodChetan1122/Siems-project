package com.siems.repository;

import com.siems.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProduct_ProductIdAndWarehouse_WarehouseId(Long productId, Long warehouseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT i FROM Inventory i
           WHERE i.product.productId = :productId AND i.warehouse.warehouseId = :warehouseId
           """)
    Optional<Inventory> findForUpdate(@Param("productId") Long productId,
                                       @Param("warehouseId") Long warehouseId);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.reorderThreshold")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.warehouse.warehouseId = :warehouseId")
    Page<Inventory> findByWarehouse_WarehouseId(@Param("warehouseId") Long warehouseId, Pageable pageable);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.warehouse.warehouseId = :warehouseId")
    long countByWarehouse_WarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("""
           SELECT COUNT(i) FROM Inventory i
           WHERE i.warehouse.warehouseId = :warehouseId AND i.quantity <= i.reorderThreshold
           """)
    long countByWarehouse_WarehouseIdAndQuantityLessThanEqualReorderThreshold(@Param("warehouseId") Long warehouseId);

    @Query("SELECT COUNT(i) FROM Inventory i")
    long countTotalSkus();

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i")
    long sumTotalUnits();

    @Query("SELECT COALESCE(SUM(i.quantity * i.product.unitPrice), 0) FROM Inventory i")
    BigDecimal sumTotalStockValue();
}
