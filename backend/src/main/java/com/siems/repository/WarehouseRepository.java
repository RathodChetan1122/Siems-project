package com.siems.repository;

import com.siems.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
           SELECT w FROM Warehouse w
           WHERE (cast(:keyword as string) IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(w.location) LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND (:activeOnly = FALSE OR w.active = TRUE)
           """)
    Page<Warehouse> search(@Param("keyword") String keyword,
                            @Param("activeOnly") boolean activeOnly,
                            Pageable pageable);
}
