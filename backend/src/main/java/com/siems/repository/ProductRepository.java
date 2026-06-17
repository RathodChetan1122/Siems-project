package com.siems.repository;

import com.siems.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("""
           SELECT p FROM Product p
           WHERE (cast(:category as string) IS NULL OR LOWER(p.category) = LOWER(:category))
             AND (cast(:keyword as string) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    Page<Product> search(@Param("keyword") String keyword,
                          @Param("category") String category,
                          Pageable pageable);
}
