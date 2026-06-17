package com.siems.service.impl;

import com.siems.dto.common.PageResponse;
import com.siems.dto.inventory.*;
import com.siems.entity.*;
import com.siems.entity.enums.MovementType;
import com.siems.exception.BadRequestException;
import com.siems.exception.InsufficientStockException;
import com.siems.exception.ResourceNotFoundException;
import com.siems.mapper.InventoryMapper;
import com.siems.mapper.StockMovementMapper;
import com.siems.repository.*;
import com.siems.security.SecurityUtils;
import com.siems.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository stockMovementRepository;
    private final LowStockAlertRepository lowStockAlertRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final InventoryMapper inventoryMapper;
    private final StockMovementMapper stockMovementMapper;

    // ==================== READ OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> getAll(Long warehouseId, Pageable pageable) {
        Page<Inventory> page = (warehouseId != null)
                ? inventoryRepository.findByWarehouse_WarehouseId(warehouseId, pageable)
                : inventoryRepository.findAll(pageable);
        return PageResponse.from(page.map(inventoryMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getById(Long inventoryId) {
        return inventoryMapper.toResponse(findInventoryOrThrow(inventoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockAlertResponse> getActiveAlerts() {
        return lowStockAlertRepository.findAllUnresolved().stream()
                .map(alert -> LowStockAlertResponse.builder()
                        .alertId(alert.getAlertId())
                        .inventoryId(alert.getInventory().getInventoryId())
                        .productName(alert.getInventory().getProduct().getName())
                        .sku(alert.getInventory().getProduct().getSku())
                        .warehouseName(alert.getInventory().getWarehouse().getName())
                        .currentQuantity(alert.getInventory().getQuantity())
                        .reorderThreshold(alert.getInventory().getReorderThreshold())
                        .createdAt(alert.getCreatedAt())
                        .build())
                .toList();
    }

    // ==================== WRITE OPERATIONS ====================

    @Override
    public InventoryResponse stockIn(StockInRequest request) {
        Inventory inventory = getOrCreateInventory(request.getProductId(), request.getWarehouseId());

        inventory.add(request.getQuantity());
        inventoryRepository.save(inventory);

        logMovement(inventory, MovementType.STOCK_IN, request.getQuantity(), "MANUAL", null, request.getReason());

        resolveAlertIfStockRestored(inventory);

        return inventoryMapper.toResponse(inventory);
    }

    @Override
    public InventoryResponse stockOut(StockOutRequest request) {
        Inventory inventory = findInventoryForUpdate(request.getProductId(), request.getWarehouseId());

        if (inventory.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Cannot remove " + request.getQuantity() + " units. Available: " + inventory.getQuantity());
        }

        inventory.deduct(request.getQuantity());
        inventoryRepository.save(inventory);

        logMovement(inventory, MovementType.STOCK_OUT, request.getQuantity(), "MANUAL", null, request.getReason());

        checkAndRaiseLowStockAlert(inventory);

        return inventoryMapper.toResponse(inventory);
    }

    @Override
    public void transferStock(StockTransferRequest request) {
        if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
            throw new BadRequestException("Source and destination warehouses must be different");
        }

        Inventory source = findInventoryForUpdate(request.getProductId(), request.getFromWarehouseId());

        if (source.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Cannot transfer " + request.getQuantity() + " units. Available at source: " + source.getQuantity());
        }

        Inventory destination = getOrCreateInventory(request.getProductId(), request.getToWarehouseId());

        source.deduct(request.getQuantity());
        inventoryRepository.save(source);
        logMovement(source, MovementType.TRANSFER_OUT, request.getQuantity(), "TRANSFER",
                request.getToWarehouseId(), "Transfer to warehouse " + request.getToWarehouseId());
        checkAndRaiseLowStockAlert(source);

        destination.add(request.getQuantity());
        inventoryRepository.save(destination);
        logMovement(destination, MovementType.TRANSFER_IN, request.getQuantity(), "TRANSFER",
                request.getFromWarehouseId(), "Transfer from warehouse " + request.getFromWarehouseId());
        resolveAlertIfStockRestored(destination);
    }

    @Override
    public InventoryResponse adjustStock(StockAdjustmentRequest request) {
        Inventory inventory = findInventoryForUpdate(request.getProductId(), request.getWarehouseId());

        int delta = request.getNewQuantity() - inventory.getQuantity();
        if (delta == 0) {
            throw new BadRequestException("New quantity is the same as current quantity — no adjustment needed");
        }

        inventory.setQuantity(request.getNewQuantity());
        inventoryRepository.save(inventory);

        logMovement(inventory, MovementType.ADJUSTMENT, Math.abs(delta), "ADJUSTMENT", null,
                request.getReason() + (delta > 0 ? " (increase)" : " (decrease)"));

        if (delta < 0) {
            checkAndRaiseLowStockAlert(inventory);
        } else {
            resolveAlertIfStockRestored(inventory);
        }

        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> getMovementHistory(Long inventoryId, String movementType, Pageable pageable) {
        MovementType type = (movementType != null && !movementType.isBlank())
                ? MovementType.valueOf(movementType.toUpperCase())
                : null;

        Page<StockMovement> page = stockMovementRepository.search(inventoryId, type, pageable);
        return PageResponse.from(page.map(stockMovementMapper::toResponse));
    }

    // ==================== RESERVATION (used by ShipmentService) ====================

    @Override
    public void reserveStock(Long productId, Long warehouseId, int quantity) {
        Inventory inventory = findInventoryForUpdate(productId, warehouseId);

        if (inventory.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product ID " + productId
                            + ". Available: " + inventory.getQuantity() + ", Required: " + quantity);
        }

        inventory.deduct(quantity);
        inventoryRepository.save(inventory);

        logMovement(inventory, MovementType.STOCK_OUT, quantity, "SHIPMENT_RESERVATION", null,
                "Reserved for outgoing shipment");

        checkAndRaiseLowStockAlert(inventory);
    }

    @Override
    public void restoreStock(Long productId, Long warehouseId, int quantity) {
        Inventory inventory = findInventoryForUpdate(productId, warehouseId);
        inventory.add(quantity);
        inventoryRepository.save(inventory);

        logMovement(inventory, MovementType.STOCK_IN, quantity, "SHIPMENT_CANCELLATION", null,
                "Stock restored due to shipment cancellation");

        resolveAlertIfStockRestored(inventory);
    }

    // ==================== PRIVATE HELPERS ====================

    private Inventory getOrCreateInventory(Long productId, Long warehouseId) {
        return inventoryRepository.findByProduct_ProductIdAndWarehouse_WarehouseId(productId, warehouseId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> ResourceNotFoundException.of("Product", productId));
                    Warehouse warehouse = warehouseRepository.findById(warehouseId)
                            .orElseThrow(() -> ResourceNotFoundException.of("Warehouse", warehouseId));

                    return inventoryRepository.save(Inventory.builder()
                            .product(product)
                            .warehouse(warehouse)
                            .quantity(0)
                            .reorderThreshold(10)
                            .build());
                });
    }

    private Inventory findInventoryForUpdate(Long productId, Long warehouseId) {
        return inventoryRepository.findForUpdate(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No inventory record for product " + productId + " in warehouse " + warehouseId));
    }

    private Inventory findInventoryOrThrow(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Inventory", inventoryId));
    }

    private void logMovement(Inventory inventory, MovementType type, int quantity,
                              String referenceType, Long referenceId, String reason) {
        StockMovement movement = StockMovement.builder()
                .inventory(inventory)
                .movementType(type)
                .quantity(quantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .reason(reason)
                .performedBy(currentUserOrNull())
                .build();
        stockMovementRepository.save(movement);
    }

    private void checkAndRaiseLowStockAlert(Inventory inventory) {
        if (!inventory.isLowStock()) return;

        boolean alreadyAlerted = lowStockAlertRepository
                .findByInventory_InventoryIdAndResolvedFalse(inventory.getInventoryId())
                .isPresent();

        if (alreadyAlerted) return;

        lowStockAlertRepository.save(LowStockAlert.builder()
                .inventory(inventory)
                .quantityAtAlert(inventory.getQuantity())
                .thresholdAtAlert(inventory.getReorderThreshold())
                .resolved(false)
                .build());

        notifyInventoryManagers(inventory);
    }

    private void resolveAlertIfStockRestored(Inventory inventory) {
        if (inventory.isLowStock()) return;

        lowStockAlertRepository.findByInventory_InventoryIdAndResolvedFalse(inventory.getInventoryId())
                .ifPresent(alert -> {
                    alert.setResolved(true);
                    alert.setResolvedAt(LocalDateTime.now());
                    lowStockAlertRepository.save(alert);
                });
    }

    private void notifyInventoryManagers(Inventory inventory) {
        List<User> recipients = userRepository.findByRole_RoleNameIn(List.of("INVENTORY_MANAGER", "ADMIN"));

        String title = "Low Stock Alert";
        String message = String.format(
                "Product '%s' (SKU: %s) at warehouse '%s' has dropped to %d units (threshold: %d).",
                inventory.getProduct().getName(),
                inventory.getProduct().getSku(),
                inventory.getWarehouse().getName(),
                inventory.getQuantity(),
                inventory.getReorderThreshold());

        for (User user : recipients) {
            notificationRepository.save(Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .type("WARNING")
                    .read(false)
                    .build());
        }
    }

    /** Returns the current authenticated user, or null if running in a non-HTTP context (e.g. background job). */
    private User currentUserOrNull() {
        try {
            return SecurityUtils.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }
}
