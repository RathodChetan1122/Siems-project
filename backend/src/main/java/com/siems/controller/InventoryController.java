package com.siems.controller;

import com.siems.dto.common.ApiResponse;
import com.siems.dto.common.PageResponse;
import com.siems.dto.inventory.*;
import com.siems.service.InventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock management, movements, and low-stock alerts")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventoryResponse>>> getAll(
            @RequestParam(required = false) Long warehouseId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAll(warehouseId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getById(id)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStockItems()));
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<List<LowStockAlertResponse>>> getActiveAlerts() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getActiveAlerts()));
    }

    @PostMapping("/stock-in")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryResponse>> stockIn(@Valid @RequestBody StockInRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock added successfully", inventoryService.stockIn(request)));
    }

    @PostMapping("/stock-out")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryResponse>> stockOut(@Valid @RequestBody StockOutRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock removed successfully", inventoryService.stockOut(request)));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> transfer(@Valid @RequestBody StockTransferRequest request) {
        inventoryService.transferStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock transferred successfully", null));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjust(@Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", inventoryService.adjustStock(request)));
    }

    @GetMapping("/{id}/movements")
    public ResponseEntity<ApiResponse<PageResponse<StockMovementResponse>>> getMovements(
            @PathVariable Long id,
            @RequestParam(required = false) String movementType,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getMovementHistory(id, movementType, pageable)));
    }
}
