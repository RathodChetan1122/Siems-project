package com.siems.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siems.dto.common.PageResponse;
import com.siems.dto.warehouse.WarehouseRequest;
import com.siems.dto.warehouse.WarehouseResponse;
import com.siems.entity.User;
import com.siems.entity.Warehouse;
import com.siems.exception.DuplicateResourceException;
import com.siems.exception.ResourceNotFoundException;
import com.siems.repository.InventoryRepository;
import com.siems.repository.UserRepository;
import com.siems.repository.WarehouseRepository;
import com.siems.service.WarehouseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public WarehouseResponse create(WarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Warehouse code already exists: " + request.getCode());
        }

        User manager = resolveManager(request.getManagerId());

        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .location(request.getLocation())
                .code(request.getCode())
                .capacity(request.getCapacity())
                .manager(manager)
                .active(true)
                .build();

        Warehouse saved = warehouseRepository.save(warehouse);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getById(Long id) {
        return toResponse(findWarehouseOrThrow(id));
    }

    @Override
    public WarehouseResponse update(Long id, WarehouseRequest request) {
        Warehouse warehouse = findWarehouseOrThrow(id);

        if (!warehouse.getCode().equals(request.getCode()) && warehouseRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Warehouse code already exists: " + request.getCode());
        }

        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setCode(request.getCode());
        warehouse.setCapacity(request.getCapacity());
        warehouse.setManager(resolveManager(request.getManagerId()));

        return toResponse(warehouseRepository.save(warehouse));
    }

    @Override
    public void deactivate(Long id) {
        Warehouse warehouse = findWarehouseOrThrow(id);
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseResponse> search(String keyword, boolean activeOnly, Pageable pageable) {
        Page<Warehouse> page = warehouseRepository.search(keyword, activeOnly, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    private User resolveManager(Long managerId) {
        if (managerId == null) return null;
        return userRepository.findById(managerId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", managerId));
    }

    private Warehouse findWarehouseOrThrow(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Warehouse", id));
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
    long totalItems = inventoryRepository.countByWarehouse_WarehouseId(warehouse.getWarehouseId());
    long lowStock = inventoryRepository.countLowStockByWarehouse(warehouse.getWarehouseId());
        return WarehouseResponse.builder()
                .warehouseId(warehouse.getWarehouseId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .code(warehouse.getCode())
                .capacity(warehouse.getCapacity())
                .managerId(warehouse.getManager() != null ? warehouse.getManager().getUserId() : null)
                .managerName(warehouse.getManager() != null ? warehouse.getManager().getUsername() : null)
                .active(warehouse.isActive())
                .totalItems(totalItems)
                .lowStockItems(lowStock)
                .createdAt(warehouse.getCreatedAt())
                .build();
    }
}
