package com.siems.service.impl;

import com.siems.dto.common.PageResponse;
import com.siems.dto.supplier.SupplierRequest;
import com.siems.dto.supplier.SupplierResponse;
import com.siems.entity.Supplier;
import com.siems.exception.DuplicateResourceException;
import com.siems.exception.ResourceNotFoundException;
import com.siems.mapper.SupplierMapper;
import com.siems.repository.SupplierRepository;
import com.siems.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    public SupplierResponse create(SupplierRequest request) {
        if (supplierRepository.existsByContactEmail(request.getContactEmail())) {
            throw new DuplicateResourceException("Supplier already exists with email: " + request.getContactEmail());
        }
        Supplier supplier = supplierMapper.toEntity(request);
        Supplier saved = supplierRepository.save(supplier);
        return supplierMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getById(Long id) {
        Supplier supplier = findSupplierOrThrow(id);
        return supplierMapper.toResponse(supplier);
    }

    @Override
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = findSupplierOrThrow(id);

        if (!supplier.getContactEmail().equalsIgnoreCase(request.getContactEmail())
                && supplierRepository.existsByContactEmail(request.getContactEmail())) {
            throw new DuplicateResourceException("Another supplier already uses email: " + request.getContactEmail());
        }

        supplierMapper.updateEntityFromRequest(request, supplier);
        Supplier updated = supplierRepository.save(supplier);
        return supplierMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Supplier supplier = findSupplierOrThrow(id);
        supplierRepository.delete(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> search(String name, String country, Pageable pageable) {
        Page<Supplier> page = supplierRepository.search(name, country, pageable);
        return PageResponse.from(page.map(supplierMapper::toResponse));
    }

    private Supplier findSupplierOrThrow(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Supplier", id));
    }
}
