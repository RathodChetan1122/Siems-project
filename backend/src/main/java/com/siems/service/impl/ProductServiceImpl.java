package com.siems.service.impl;

import com.siems.dto.common.PageResponse;
import com.siems.dto.product.ProductRequest;
import com.siems.dto.product.ProductResponse;
import com.siems.entity.Product;
import com.siems.entity.Supplier;
import com.siems.exception.DuplicateResourceException;
import com.siems.exception.ResourceNotFoundException;
import com.siems.mapper.ProductMapper;
import com.siems.repository.ProductRepository;
import com.siems.repository.SupplierRepository;
import com.siems.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product already exists with SKU: " + request.getSku());
        }

        Product product = productMapper.toEntity(request);

        if (request.getUnitOfMeasure() == null || request.getUnitOfMeasure().isBlank()) {
            product.setUnitOfMeasure("PCS");
        }

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Supplier", request.getSupplierId()));
            product.setSupplier(supplier);
        }

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productMapper.toResponse(findProductOrThrow(id));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);

        if (!product.getSku().equalsIgnoreCase(request.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Another product already uses SKU: " + request.getSku());
        }

        productMapper.updateEntityFromRequest(request, product);

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Supplier", request.getSupplierId()));
            product.setSupplier(supplier);
        } else {
            product.setSupplier(null);
        }

        Product updated = productRepository.save(product);
        return productMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, String category, Pageable pageable) {
        Page<Product> page = productRepository.search(keyword, category, pageable);
        return PageResponse.from(page.map(productMapper::toResponse));
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
    }
}
