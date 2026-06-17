package com.siems.service;

import com.siems.dto.common.PageResponse;
import com.siems.dto.product.ProductRequest;
import com.siems.dto.product.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse getById(Long id);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
    PageResponse<ProductResponse> search(String keyword, String category, Pageable pageable);
}
