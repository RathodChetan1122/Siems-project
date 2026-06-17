package com.siems.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siems.dto.shipment.ShipmentItemRequest;
import com.siems.dto.shipment.ShipmentRequest;
import com.siems.dto.shipment.ShipmentStatusUpdateRequest;
import com.siems.entity.*;
import com.siems.repository.*;
import com.siems.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("End-to-End Shipment Workflow Integration Test")
class ShipmentWorkflowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private ShipmentStatusRepository shipmentStatusRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String managerToken;
    private Long supplierId, customerId, productId, warehouseId;

    @BeforeEach
    void setUp() {
        Role exportManagerRole = roleRepository.findByRoleName("EXPORT_MANAGER")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("EXPORT_MANAGER").build()));

        User manager = userRepository.save(User.builder()
                .username("export_test")
                .email("export_test@siems.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(exportManagerRole)
                .enabled(true)
                .build());
        managerToken = "Bearer " + jwtService.generateAccessToken(manager);

        for (String statusName : List.of("PENDING", "PACKED", "DISPATCHED", "IN_TRANSIT", "AT_CUSTOMS", "DELIVERED", "CANCELLED")) {
            shipmentStatusRepository.findByStatusName(statusName)
                    .orElseGet(() -> shipmentStatusRepository.save(ShipmentStatus.builder().statusName(statusName).build()));
        }

        Supplier supplier = supplierRepository.save(Supplier.builder()
                .name("Test Supplier").country("India").contactEmail("supplier@test.com")
                .rating(BigDecimal.valueOf(4.0)).build());
        supplierId = supplier.getSupplierId();

        Customer customer = customerRepository.save(Customer.builder()
                .name("Test Customer").billingAddress("Addr 1").shippingAddress("Addr 1")
                .contactEmail("customer@test.com").build());
        customerId = customer.getCustomerId();

        Product product = productRepository.save(Product.builder()
                .sku("TEST-SKU-001").name("Test Product").unitOfMeasure("PCS")
                .unitPrice(BigDecimal.valueOf(10.00)).supplier(supplier).build());
        productId = product.getProductId();

        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
                .name("Test Warehouse").location("Test City").code("WH-TEST-01").build());
        warehouseId = warehouse.getWarehouseId();

        inventoryRepository.save(Inventory.builder()
                .product(product).warehouse(warehouse)
                .quantity(500).reorderThreshold(50).build());
    }

    @Test
    @DisplayName("Full shipment lifecycle: create -> PACKED -> DISPATCHED -> IN_TRANSIT -> DELIVERED")
    void fullShipmentLifecycleShouldSucceed() throws Exception {

        ShipmentItemRequest itemRequest = new ShipmentItemRequest();
        itemRequest.setProductId(productId);
        itemRequest.setQuantity(100);
        itemRequest.setUnitPrice(BigDecimal.valueOf(10.00));

        ShipmentRequest createRequest = new ShipmentRequest();
        createRequest.setSupplierId(supplierId);
        createRequest.setCustomerId(customerId);
        createRequest.setWarehouseId(warehouseId);
        createRequest.setCarrier("Test Carrier");
        createRequest.setEta(LocalDate.now().plusDays(15));
        createRequest.setItems(List.of(itemRequest));

        String createResponse = mockMvc.perform(post("/api/v1/shipments")
                        .header("Authorization", managerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.currentStatus", is("PENDING")))
                .andReturn().getResponse().getContentAsString();

        Long shipmentId = objectMapper.readTree(createResponse).path("data").path("shipmentId").asLong();

        Inventory afterReserve = inventoryRepository
                .findByProduct_ProductIdAndWarehouse_WarehouseId(productId, warehouseId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(afterReserve.getQuantity()).isEqualTo(400);

        updateStatus(shipmentId, "PACKED");
        updateStatus(shipmentId, "DISPATCHED");
        updateStatus(shipmentId, "IN_TRANSIT");
        updateStatus(shipmentId, "DELIVERED");

        mockMvc.perform(get("/api/v1/shipments/" + shipmentId + "/tracking")
                        .header("Authorization", managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus", is("DELIVERED")))
                .andExpect(jsonPath("$.data.timeline", org.hamcrest.Matchers.hasSize(5)));
    }

    @Test
    @DisplayName("Cancelling a PENDING shipment should restore reserved inventory")
    void cancellingPendingShipmentShouldRestoreInventory() throws Exception {
        ShipmentItemRequest itemRequest = new ShipmentItemRequest();
        itemRequest.setProductId(productId);
        itemRequest.setQuantity(150);
        itemRequest.setUnitPrice(BigDecimal.valueOf(10.00));

        ShipmentRequest createRequest = new ShipmentRequest();
        createRequest.setSupplierId(supplierId);
        createRequest.setCustomerId(customerId);
        createRequest.setWarehouseId(warehouseId);
        createRequest.setEta(LocalDate.now().plusDays(10));
        createRequest.setItems(List.of(itemRequest));

        String createResponse = mockMvc.perform(post("/api/v1/shipments")
                        .header("Authorization", managerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long shipmentId = objectMapper.readTree(createResponse).path("data").path("shipmentId").asLong();

        Inventory afterReserve = inventoryRepository
                .findByProduct_ProductIdAndWarehouse_WarehouseId(productId, warehouseId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(afterReserve.getQuantity()).isEqualTo(350);

        updateStatus(shipmentId, "CANCELLED");

        Inventory afterCancel = inventoryRepository
                .findByProduct_ProductIdAndWarehouse_WarehouseId(productId, warehouseId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(afterCancel.getQuantity()).isEqualTo(500);
    }

    private void updateStatus(Long shipmentId, String status) throws Exception {
        ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
        request.setStatus(status);

        mockMvc.perform(patch("/api/v1/shipments/" + shipmentId + "/status")
                        .header("Authorization", managerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus", is(status)));
    }
}
