package com.siems.repository;

import com.siems.entity.StockMovement;
import com.siems.entity.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("""
           SELECT m FROM StockMovement m
           WHERE (cast(:inventoryId as long) IS NULL OR m.inventory.inventoryId = :inventoryId)
             AND (cast(:movementType as string) IS NULL OR m.movementType = :movementType)
           ORDER BY m.createdAt DESC
           """)
    Page<StockMovement> search(@Param("inventoryId") Long inventoryId,
                                @Param("movementType") MovementType movementType,
                                Pageable pageable);
}
