package com.siems.repository;

import com.siems.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByContactEmail(String email);

    @Query("""
           SELECT c FROM Customer c
           WHERE cast(:keyword as string) IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(c.contactEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
           """)
    Page<Customer> search(@Param("keyword") String keyword, Pageable pageable);
}
