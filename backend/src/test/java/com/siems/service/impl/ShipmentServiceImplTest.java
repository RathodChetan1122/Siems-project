package com.siems.service.impl;

import com.siems.dto.shipment.*;
import com.siems.entity.*;
import com.siems.exception.BadRequestException;
import com.siems.exception.ResourceNotFoundException;
import com.siems.mapper.ShipmentMapper;
import com.siems.repository.*;
import com.siems.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShipmentServiceImpl Unit Tests")
class ShipmentServiceImplTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private ShipmentStatusRepository shipmentStatusRepository;
    @Mock private ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;
    @Mock private InventoryService inventoryService;
    @Mock private ShipmentMapper shipmentMapper;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    private Shipment shipment;
    private ShipmentStatus pendingStatus;
    private ShipmentStatus packedStatus;
    private ShipmentStatus deliveredStatus;
    private ShipmentStatus cancelledStatus;

    @BeforeEach
    void setUp() {
        pendingStatus = ShipmentStatus.builder().statusId(1L).statusName("PENDING").build();
        packedStatus = ShipmentStatus.builder().statusId(2L).statusName("PACKED").build();
        deliveredStatus = ShipmentStatus.builder().statusId(6L).statusName("DELIVERED").build();
        cancelledStatus = ShipmentStatus.builder().statusId(7L).statusName("CANCELLED").build();

        shipment = Shipment.builder()
                .shipmentId(12L)
                .trackingNumber("SIEMS-SHP-A1B2C3D4")
                .currentStatus(pendingStatus)
                .warehouse(Warehouse.builder().warehouseId(1L).build())
                .items(List.of(
                        ShipmentItem.builder()
                                .product(Product.builder().productId(1L).sku("TEX-COT-001").build())
                                .quantity(100)
                                .unitPrice(BigDecimal.valueOf(4.50))
                                .build()
                ))
                .build();
    }

    @Nested
    @DisplayName("updateStatus() — Valid Transitions")
    class ValidTransitions {

        @Test
        @DisplayName("Should transition PENDING -> PACKED successfully")
        void shouldTransitionPendingToPacked() {
            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus("PACKED");
            request.setLocation("Central Warehouse Hyderabad");
            request.setRemarks("All items packed");

            when(shipmentRepository.findById(12L)).thenReturn(Optional.of(shipment));
            when(shipmentStatusRepository.findByStatusName("PACKED")).thenReturn(Optional.of(packedStatus));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
            when(shipmentMapper.toResponse(any())).thenReturn(mock(ShipmentResponse.class));

            shipmentService.updateStatus(12L, request);

            assertThat(shipment.getCurrentStatus().getStatusName()).isEqualTo("PACKED");
            verify(shipmentStatusHistoryRepository).save(any(ShipmentStatusHistory.class));
        }

        @Test
        @DisplayName("Should transition IN_TRANSIT -> DELIVERED successfully")
        void shouldTransitionInTransitToDelivered() {
            ShipmentStatus inTransitStatus = ShipmentStatus.builder().statusId(4L).statusName("IN_TRANSIT").build();
            shipment.setCurrentStatus(inTransitStatus);

            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus("DELIVERED");

            when(shipmentRepository.findById(12L)).thenReturn(Optional.of(shipment));
            when(shipmentStatusRepository.findByStatusName("DELIVERED")).thenReturn(Optional.of(deliveredStatus));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
            when(shipmentMapper.toResponse(any())).thenReturn(mock(ShipmentResponse.class));

            shipmentService.updateStatus(12L, request);

            assertThat(shipment.getCurrentStatus().getStatusName()).isEqualTo("DELIVERED");
        }
    }

    @Nested
    @DisplayName("updateStatus() — Invalid Transitions")
    class InvalidTransitions {

        @Test
        @DisplayName("Should reject PENDING -> DELIVERED (skipping intermediate states)")
        void shouldRejectInvalidJumpFromPendingToDelivered() {
            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus("DELIVERED");

            when(shipmentRepository.findById(12L)).thenReturn(Optional.of(shipment));

            assertThatThrownBy(() -> shipmentService.updateStatus(12L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid status transition")
                    .hasMessageContaining("PENDING -> DELIVERED");

            verify(shipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject transition from terminal DELIVERED state")
        void shouldRejectTransitionFromDeliveredState() {
            shipment.setCurrentStatus(deliveredStatus);

            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus("IN_TRANSIT");

            when(shipmentRepository.findById(12L)).thenReturn(Optional.of(shipment));

            assertThatThrownBy(() -> shipmentService.updateStatus(12L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("terminal state");
        }

        @Test
        @DisplayName("Should reject transition to the same status")
        void shouldRejectTransitionToSameStatus() {
            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus("PENDING");

            when(shipmentRepository.findById(12L)).thenReturn(Optional.of(shipment));

            assertThatThrownBy(() -> shipmentService.updateStatus(12L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already in status");
        }

        @Test
        @DisplayName("Should reject unknown status value")
        void shouldRejectUnknownStatusValue() {
            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus("SHIPPED");

            when(shipmentRepository.findById(12L)).thenReturn(Optional.of(shipment));

            assertThatThrownBy(() -> shipmentService.updateStatus(12L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid shipment status");
        }
    }

    @Nested
    @DisplayName("updateStatus() — Cancellation Side Effects")
    class CancellationSideEffects {

        @Test
        @DisplayName("Should restore inventory when cancelling from PENDING")
        void shouldRestoreInventoryWhenCancellingFromPending() {
            ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
            request.setStatus("CANCELLED");
            request.setRemarks("Customer cancelled order");

            when(shipmentRepository.findById(12L)).thenReturn(Optional.of(shipment));
            when(shipmentStatusRepository.findByStatusName("CANCELLED")).thenReturn(Optional.of(cancelledStatus));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
            when(shipmentMapper.toResponse(any())).thenReturn(mock(ShipmentResponse.class));

            shipmentService.updateStatus(12L, request);

            verify(inventoryService, times(1)).restoreStock(eq(1L), anyLong(), eq(100));
            assertThat(shipment.getCurrentStatus().getStatusName()).isEqualTo("CANCELLED");
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateShipment {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when supplier does not exist")
        void shouldThrowWhenSupplierNotFound() {
            ShipmentRequest request = new ShipmentRequest();
            request.setSupplierId(999L);
            request.setCustomerId(1L);
            request.setWarehouseId(1L);
            request.setItems(List.of(new ShipmentItemRequest()));

            when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shipmentService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Supplier");
        }
    }
}
