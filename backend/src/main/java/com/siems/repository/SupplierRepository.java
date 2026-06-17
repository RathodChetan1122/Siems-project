package com.siems.repository;

import com.siems.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByContactEmail(String email);

    @Query("""
           SELECT s FROM Supplier s
           WHERE (cast(:name as string) IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
             AND (cast(:country as string) IS NULL OR LOWER(s.country) = LOWER(:country))
           """)
    Page<Supplier> search(@Param("name") String name,
                           @Param("country") String country,
                           Pageable pageable);
}
