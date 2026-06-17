package com.siems.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.siems.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByContactEmail(String email);

    @Query(value = """
           SELECT s.* FROM suppliers s
           WHERE (COALESCE(:name, '') = ''
                  OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
             AND (COALESCE(:country, '') = ''
                  OR LOWER(s.country) = LOWER(:country))
           """,
           countQuery = """
           SELECT COUNT(*) FROM suppliers s
           WHERE (COALESCE(:name, '') = ''
                  OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
             AND (COALESCE(:country, '') = ''
                  OR LOWER(s.country) = LOWER(:country))
           """,
           nativeQuery = true)
    Page<Supplier> search(@Param("name") String name,
                           @Param("country") String country,
                           Pageable pageable);
}