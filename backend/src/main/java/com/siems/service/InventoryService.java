package com.siems.service;

import com.siems.dto.common.PageResponse;
import com.siems.dto.inventory.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {

    PageResponse<InventoryResponse> getAll(Long warehouseId, Pageable pageable);
    InventoryResponse getById(Long inventoryId);
    List<InventoryResponse> getLowStockItems();
    List<LowStockAlertResponse> getActiveAlerts();

    InventoryResponse stockIn(StockInRequest request);
    InventoryResponse stockOut(StockOutRequest request);
    void transferStock(StockTransferRequest request);
    InventoryResponse adjustStock(StockAdjustmentRequest request);

    PageResponse<StockMovementResponse> getMovementHistory(Long inventoryId, String movementType, Pageable pageable);

    void reserveStock(Long productId, Long warehouseId, int quantity);
    void restoreStock(Long productId, Long warehouseId, int quantity);
}
