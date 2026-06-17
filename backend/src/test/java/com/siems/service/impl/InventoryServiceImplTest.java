package com.siems.service.impl;

import com.siems.dto.inventory.*;
import com.siems.entity.*;
import com.siems.entity.enums.MovementType;
import com.siems.exception.BadRequestException;
import com.siems.exception.InsufficientStockException;
import com.siems.mapper.InventoryMapper;
import com.siems.mapper.StockMovementMapper;
import com.siems.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryServiceImpl Unit Tests")
class InventoryServiceImplTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private LowStockAlertRepository lowStockAlertRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private InventoryMapper inventoryMapper;
    @Mock private StockMovementMapper stockMovementMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory inventory;
    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        product = Product.builder().productId(2L).sku("ELEC-BT-002").name("Bluetooth Earphones").build();
        warehouse = Warehouse.builder().warehouseId(2L).name("Port Warehouse Mumbai").build();

        inventory = Inventory.builder()
                .inventoryId(10L)
                .product(product)
                .warehouse(warehouse)
                .quantity(100)
                .reorderThreshold(50)
                .build();
    }

    @Nested
    @DisplayName("stockOut()")
    class StockOut {

        @Test
        @DisplayName("Should deduct stock successfully when sufficient quantity available")
        void shouldDeductStockSuccessfully() {
            StockOutRequest request = new StockOutRequest();
            request.setProductId(2L);
            request.setWarehouseId(2L);
            request.setQuantity(40);
            request.setReason("Damaged goods write-off");

            when(inventoryRepository.findForUpdate(2L, 2L)).thenReturn(Optional.of(inventory));
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
            when(inventoryMapper.toResponse(any())).thenReturn(mock(InventoryResponse.class));
            when(lowStockAlertRepository.findByInventory_InventoryIdAndResolvedFalse(10L)).thenReturn(Optional.empty());

            inventoryService.stockOut(request);

            assertThat(inventory.getQuantity()).isEqualTo(60);
            verify(stockMovementRepository).save(argThat(movement ->
                    movement.getMovementType() == MovementType.STOCK_OUT
                            && movement.getQuantity() == 40
            ));
        }

        @Test
        @DisplayName("Should throw InsufficientStockException when quantity exceeds available stock")
        void shouldThrowWhenInsufficientStock() {
            StockOutRequest request = new StockOutRequest();
            request.setProductId(2L);
            request.setWarehouseId(2L);
            request.setQuantity(150);

            when(inventoryRepository.findForUpdate(2L, 2L)).thenReturn(Optional.of(inventory));

            assertThatThrownBy(() -> inventoryService.stockOut(request))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Available: 100");

            assertThat(inventory.getQuantity()).isEqualTo(100);
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should raise a low-stock alert when stock drops to or below threshold")
        void shouldRaiseLowStockAlertWhenThresholdReached() {
            StockOutRequest request = new StockOutRequest();
            request.setProductId(2L);
            request.setWarehouseId(2L);
            request.setQuantity(55);

            when(inventoryRepository.findForUpdate(2L, 2L)).thenReturn(Optional.of(inventory));
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
            when(inventoryMapper.toResponse(any())).thenReturn(mock(InventoryResponse.class));
            when(lowStockAlertRepository.findByInventory_InventoryIdAndResolvedFalse(10L)).thenReturn(Optional.empty());
            when(userRepository.findByRole_RoleNameIn(List.of("INVENTORY_MANAGER", "ADMIN"))).thenReturn(List.of());

            inventoryService.stockOut(request);

            assertThat(inventory.getQuantity()).isEqualTo(45);
            assertThat(inventory.isLowStock()).isTrue();

            ArgumentCaptor<LowStockAlert> alertCaptor = ArgumentCaptor.forClass(LowStockAlert.class);
            verify(lowStockAlertRepository).save(alertCaptor.capture());
            assertThat(alertCaptor.getValue().getQuantityAtAlert()).isEqualTo(45);
            assertThat(alertCaptor.getValue().isResolved()).isFalse();
        }

        @Test
        @DisplayName("Should NOT raise duplicate alert when one is already active")
        void shouldNotRaiseDuplicateAlert() {
            inventory.setQuantity(60);

            StockOutRequest request = new StockOutRequest();
            request.setProductId(2L);
            request.setWarehouseId(2L);
            request.setQuantity(20);

            LowStockAlert existingAlert = LowStockAlert.builder().alertId(1L).resolved(false).build();

            when(inventoryRepository.findForUpdate(2L, 2L)).thenReturn(Optional.of(inventory));
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
            when(inventoryMapper.toResponse(any())).thenReturn(mock(InventoryResponse.class));
            when(lowStockAlertRepository.findByInventory_InventoryIdAndResolvedFalse(10L))
                    .thenReturn(Optional.of(existingAlert));

            inventoryService.stockOut(request);

            verify(lowStockAlertRepository, never()).save(any(LowStockAlert.class));
        }
    }

    @Nested
    @DisplayName("stockIn()")
    class StockIn {

        @Test
        @DisplayName("Should resolve active low-stock alert when stock is replenished above threshold")
        void shouldResolveAlertWhenStockReplenished() {
            inventory.setQuantity(30);

            StockInRequest request = new StockInRequest();
            request.setProductId(2L);
            request.setWarehouseId(2L);
            request.setQuantity(100);
            request.setReason("Restocked from supplier");

            LowStockAlert activeAlert = LowStockAlert.builder().alertId(1L).resolved(false).build();

            when(inventoryRepository.findByProduct_ProductIdAndWarehouse_WarehouseId(2L, 2L))
                    .thenReturn(Optional.of(inventory));
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
            when(inventoryMapper.toResponse(any())).thenReturn(mock(InventoryResponse.class));
            when(lowStockAlertRepository.findByInventory_InventoryIdAndResolvedFalse(10L))
                    .thenReturn(Optional.of(activeAlert));

            inventoryService.stockIn(request);

            assertThat(inventory.getQuantity()).isEqualTo(130);

            ArgumentCaptor<LowStockAlert> alertCaptor = ArgumentCaptor.forClass(LowStockAlert.class);
            verify(lowStockAlertRepository).save(alertCaptor.capture());
            assertThat(alertCaptor.getValue().isResolved()).isTrue();
            assertThat(alertCaptor.getValue().getResolvedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("transferStock()")
    class TransferStock {

        @Test
        @DisplayName("Should transfer stock between two warehouses successfully")
        void shouldTransferStockBetweenWarehouses() {
            Warehouse warehouse1 = Warehouse.builder().warehouseId(1L).name("Warehouse 1").build();
            Inventory sourceInventory = Inventory.builder()
                    .inventoryId(10L).product(product).warehouse(warehouse).quantity(100).reorderThreshold(50).build();
            Inventory destInventory = Inventory.builder()
                    .inventoryId(11L).product(product).warehouse(warehouse1).quantity(20).reorderThreshold(10).build();

            StockTransferRequest request = new StockTransferRequest();
            request.setProductId(2L);
            request.setFromWarehouseId(2L);
            request.setToWarehouseId(1L);
            request.setQuantity(30);

            when(inventoryRepository.findForUpdate(2L, 2L)).thenReturn(Optional.of(sourceInventory));
            when(inventoryRepository.findByProduct_ProductIdAndWarehouse_WarehouseId(2L, 1L))
                    .thenReturn(Optional.of(destInventory));
            when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
            when(lowStockAlertRepository.findByInventory_InventoryIdAndResolvedFalse(any())).thenReturn(Optional.empty());

            inventoryService.transferStock(request);

            assertThat(sourceInventory.getQuantity()).isEqualTo(70);
            assertThat(destInventory.getQuantity()).isEqualTo(50);

            verify(stockMovementRepository).save(argThat(m -> m.getMovementType() == MovementType.TRANSFER_OUT && m.getQuantity() == 30));
            verify(stockMovementRepository).save(argThat(m -> m.getMovementType() == MovementType.TRANSFER_IN && m.getQuantity() == 30));
        }

        @Test
        @DisplayName("Should throw BadRequestException when source and destination warehouses are the same")
        void shouldThrowWhenSourceAndDestinationSame() {
            StockTransferRequest request = new StockTransferRequest();
            request.setProductId(2L);
            request.setFromWarehouseId(2L);
            request.setToWarehouseId(2L);
            request.setQuantity(10);

            assertThatThrownBy(() -> inventoryService.transferStock(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("must be different");

            verify(inventoryRepository, never()).findForUpdate(any(), any());
        }
    }

    @Nested
    @DisplayName("reserveStock() / restoreStock()")
    class ReservationOperations {

        @Test
        @DisplayName("reserveStock() should deduct quantity and log SHIPMENT_RESERVATION movement")
        void reserveStockShouldDeductAndLog() {
            when(inventoryRepository.findForUpdate(2L, 2L)).thenReturn(Optional.of(inventory));
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
            when(lowStockAlertRepository.findByInventory_InventoryIdAndResolvedFalse(10L)).thenReturn(Optional.empty());

            inventoryService.reserveStock(2L, 2L, 30);

            assertThat(inventory.getQuantity()).isEqualTo(70);
            verify(stockMovementRepository).save(argThat(m ->
                    m.getMovementType() == MovementType.STOCK_OUT
                            && "SHIPMENT_RESERVATION".equals(m.getReferenceType())
            ));
        }

        @Test
        @DisplayName("restoreStock() should add quantity and resolve alert if applicable")
        void restoreStockShouldAddAndResolveAlert() {
            inventory.setQuantity(40);

            LowStockAlert activeAlert = LowStockAlert.builder().alertId(1L).resolved(false).build();

            when(inventoryRepository.findForUpdate(2L, 2L)).thenReturn(Optional.of(inventory));
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
            when(lowStockAlertRepository.findByInventory_InventoryIdAndResolvedFalse(10L))
                    .thenReturn(Optional.of(activeAlert));

            inventoryService.restoreStock(2L, 2L, 30);

            assertThat(inventory.getQuantity()).isEqualTo(70);
            verify(stockMovementRepository).save(argThat(m ->
                    m.getMovementType() == MovementType.STOCK_IN
                            && "SHIPMENT_CANCELLATION".equals(m.getReferenceType())
            ));
            assertThat(activeAlert.isResolved()).isTrue();
        }
    }
}
