package com.siems.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.siems.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByContactEmail(String email);

    @Query(value = """
           SELECT c.* FROM customers c
           WHERE (COALESCE(:keyword, '') = ''
                  OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(c.contact_email) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """,
           countQuery = """
           SELECT COUNT(*) FROM customers c
           WHERE (COALESCE(:keyword, '') = ''
                  OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(c.contact_email) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """,
           nativeQuery = true)
    Page<Customer> search(@Param("keyword") String keyword, Pageable pageable);
}