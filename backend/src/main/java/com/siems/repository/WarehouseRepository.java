package com.siems.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.siems.entity.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByCode(String code);

    boolean existsByCode(String code);

    @Query(value = """
           SELECT w.* FROM warehouses w
           WHERE (COALESCE(:keyword, '') = ''
                  OR LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(w.location) LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND (:activeOnly = FALSE OR w.is_active = TRUE)
           """,
           countQuery = """
           SELECT COUNT(*) FROM warehouses w
           WHERE (COALESCE(:keyword, '') = ''
                  OR LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(w.location) LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND (:activeOnly = FALSE OR w.is_active = TRUE)
           """,
           nativeQuery = true)
    Page<Warehouse> search(@Param("keyword") String keyword,
                            @Param("activeOnly") boolean activeOnly,
                            Pageable pageable);
}