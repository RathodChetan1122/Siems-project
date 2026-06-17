# SIEMS — Complete Project Folder Tree
siems/

│

├── .env.example                          # Environment variable template

├── .github/

│   └── workflows/

│       └── ci.yml                        # GitHub Actions CI/CD pipeline

├── docker-compose.yml                    # Full stack Docker Compose

├── README.md                             # Main documentation

├── SETUP.md                              # Local development setup guide

├── FOLDER_TREE.md                        # This file

│

├── database/                             # Standalone SQL reference files

│   ├── schema.sql

│   └── data.sql

│

├── backend/                              # Spring Boot 3 / Java 17

│   ├── .dockerignore

│   ├── Dockerfile                        # Multi-stage build (Maven → JRE Alpine)

│   ├── pom.xml                           # Maven dependencies

│   │

│   └── src/

│       ├── main/

│       │   ├── java/com/siems/

│       │   │   ├── SiemsApplication.java

│       │   │   │

│       │   │   ├── config/

│       │   │   │   ├── CorsConfig.java

│       │   │   │   ├── DataSeeder.java

│       │   │   │   ├── OpenApiConfig.java

│       │   │   │   ├── PasswordEncoderConfig.java

│       │   │   │   └── SecurityConfig.java

│       │   │   │

│       │   │   ├── controller/

│       │   │   │   ├── AnalyticsController.java

│       │   │   │   ├── AuthController.java

│       │   │   │   ├── CustomerController.java

│       │   │   │   ├── InventoryController.java

│       │   │   │   ├── NotificationController.java

│       │   │   │   ├── ProductController.java

│       │   │   │   ├── ShipmentController.java

│       │   │   │   ├── SupplierController.java

│       │   │   │   └── WarehouseController.java

│       │   │   │

│       │   │   ├── dto/

│       │   │   │   ├── analytics/

│       │   │   │   │   └── DashboardSummaryResponse.java

│       │   │   │   ├── auth/

│       │   │   │   │   ├── AuthResponse.java

│       │   │   │   │   ├── LoginRequest.java

│       │   │   │   │   ├── RefreshTokenRequest.java

│       │   │   │   │   └── RegisterRequest.java

│       │   │   │   ├── common/

│       │   │   │   │   ├── ApiResponse.java

│       │   │   │   │   ├── ErrorResponse.java

│       │   │   │   │   └── PageResponse.java

│       │   │   │   ├── customer/

│       │   │   │   │   ├── CustomerRequest.java

│       │   │   │   │   └── CustomerResponse.java

│       │   │   │   ├── inventory/

│       │   │   │   │   ├── InventoryRequest.java

│       │   │   │   │   ├── InventoryResponse.java

│       │   │   │   │   ├── LowStockAlertResponse.java

│       │   │   │   │   ├── StockAdjustmentRequest.java

│       │   │   │   │   ├── StockInRequest.java

│       │   │   │   │   ├── StockMovementResponse.java

│       │   │   │   │   ├── StockOutRequest.java

│       │   │   │   │   └── StockTransferRequest.java

│       │   │   │   ├── notification/

│       │   │   │   │   └── NotificationResponse.java

│       │   │   │   ├── product/

│       │   │   │   │   ├── ProductRequest.java

│       │   │   │   │   └── ProductResponse.java

│       │   │   │   ├── shipment/

│       │   │   │   │   ├── ShipmentItemRequest.java

│       │   │   │   │   ├── ShipmentRequest.java

│       │   │   │   │   ├── ShipmentResponse.java

│       │   │   │   │   ├── ShipmentStatusUpdateRequest.java

│       │   │   │   │   └── ShipmentTrackingResponse.java

│       │   │   │   ├── supplier/

│       │   │   │   │   ├── SupplierRequest.java

│       │   │   │   │   └── SupplierResponse.java

│       │   │   │   └── warehouse/

│       │   │   │       ├── WarehouseRequest.java

│       │   │   │       └── WarehouseResponse.java

│       │   │   │

│       │   │   ├── entity/

│       │   │   │   ├── enums/

│       │   │   │   │   ├── MovementType.java

│       │   │   │   │   └── ShipmentStatusEnum.java

│       │   │   │   ├── Customer.java

│       │   │   │   ├── Inventory.java

│       │   │   │   ├── LowStockAlert.java

│       │   │   │   ├── Notification.java

│       │   │   │   ├── Product.java

│       │   │   │   ├── RefreshToken.java

│       │   │   │   ├── Role.java

│       │   │   │   ├── Shipment.java

│       │   │   │   ├── ShipmentItem.java

│       │   │   │   ├── ShipmentStatus.java

│       │   │   │   ├── ShipmentStatusHistory.java

│       │   │   │   ├── StockMovement.java

│       │   │   │   ├── Supplier.java

│       │   │   │   ├── User.java

│       │   │   │   └── Warehouse.java

│       │   │   │

│       │   │   ├── exception/

│       │   │   │   ├── BadRequestException.java

│       │   │   │   ├── DuplicateResourceException.java

│       │   │   │   ├── GlobalExceptionHandler.java

│       │   │   │   ├── InsufficientStockException.java

│       │   │   │   ├── ResourceNotFoundException.java

│       │   │   │   └── UnauthorizedException.java

│       │   │   │

│       │   │   ├── mapper/

│       │   │   │   ├── CustomerMapper.java

│       │   │   │   ├── InventoryMapper.java

│       │   │   │   ├── ProductMapper.java

│       │   │   │   ├── ShipmentMapper.java

│       │   │   │   ├── StockMovementMapper.java

│       │   │   │   └── SupplierMapper.java

│       │   │   │

│       │   │   ├── repository/

│       │   │   │   ├── CustomerRepository.java

│       │   │   │   ├── InventoryRepository.java

│       │   │   │   ├── LowStockAlertRepository.java

│       │   │   │   ├── NotificationRepository.java

│       │   │   │   ├── ProductRepository.java

│       │   │   │   ├── RefreshTokenRepository.java

│       │   │   │   ├── RoleRepository.java

│       │   │   │   ├── ShipmentItemRepository.java

│       │   │   │   ├── ShipmentRepository.java

│       │   │   │   ├── ShipmentStatusHistoryRepository.java

│       │   │   │   ├── ShipmentStatusRepository.java

│       │   │   │   ├── StockMovementRepository.java

│       │   │   │   ├── SupplierRepository.java

│       │   │   │   ├── UserRepository.java

│       │   │   │   └── WarehouseRepository.java

│       │   │   │

│       │   │   ├── security/

│       │   │   │   ├── CustomUserDetailsService.java

│       │   │   │   ├── JwtAccessDeniedHandler.java

│       │   │   │   ├── JwtAuthenticationEntryPoint.java

│       │   │   │   ├── JwtAuthenticationFilter.java

│       │   │   │   ├── JwtService.java

│       │   │   │   └── SecurityUtils.java

│       │   │   │

│       │   │   └── service/

│       │   │       ├── impl/

│       │   │       │   ├── AnalyticsServiceImpl.java

│       │   │       │   ├── AuthServiceImpl.java

│       │   │       │   ├── CustomerServiceImpl.java

│       │   │       │   ├── InventoryServiceImpl.java

│       │   │       │   ├── NotificationServiceImpl.java

│       │   │       │   ├── ProductServiceImpl.java

│       │   │       │   ├── ShipmentServiceImpl.java

│       │   │       │   ├── SupplierServiceImpl.java

│       │   │       │   └── WarehouseServiceImpl.java

│       │   │       ├── AnalyticsService.java

│       │   │       ├── AuthService.java

│       │   │       ├── CustomerService.java

│       │   │       ├── InventoryService.java

│       │   │       ├── NotificationService.java

│       │   │       ├── ProductService.java

│       │   │       ├── ShipmentService.java

│       │   │       ├── SupplierService.java

│       │   │       └── WarehouseService.java

│       │   │

│       │   └── resources/

│       │       ├── application.yml

│       │       ├── application-test.yml

│       │       └── db/migration/

│       │           ├── V1__init_schema.sql

│       │           └── V2__seed_data.sql

│       │

│       └── test/

│           ├── java/com/siems/

│           │   ├── SiemsApplicationTests.java

│           │   ├── controller/

│           │   │   ├── AuthControllerIntegrationTest.java

│           │   │   └── ShipmentWorkflowIntegrationTest.java

│           │   ├── entity/

│           │   │   ├── enums/

│           │   │   │   └── ShipmentStatusEnumTest.java

│           │   │   └── InventoryTest.java

│           │   └── service/impl/

│           │       ├── InventoryServiceImplTest.java

│           │       ├── ShipmentServiceImplTest.java

│           │       └── SupplierServiceImplTest.java

│           └── resources/

│               └── application-test.yml

│

└── frontend/                             # React 18 + Tailwind CSS

├── .dockerignore

├── Dockerfile                        # Multi-stage (Node → Nginx Alpine)

├── nginx.conf                        # SPA routing + API proxy

├── index.html

├── package.json

├── vite.config.js

├── tailwind.config.js

├── postcss.config.js

│

└── src/

├── main.jsx

├── App.jsx                       # Router + AuthProvider

├── index.css                     # Tailwind + global utility classes

│

├── api/

│   ├── analyticsApi.js

│   ├── authApi.js

│   ├── axiosInstance.js          # Interceptors + auto-refresh

│   ├── customerApi.js

│   ├── inventoryApi.js

│   ├── notificationApi.js

│   ├── productApi.js

│   ├── shipmentApi.js

│   ├── supplierApi.js

│   └── warehouseApi.js

│

├── context/

│   └── AuthContext.jsx           # Auth state + localStorage sync

│

├── components/

│   ├── common/

│   │   ├── ConfirmDialog.jsx

│   │   ├── EmptyState.jsx

│   │   ├── KpiCard.jsx

│   │   ├── LoadingSpinner.jsx

│   │   ├── Modal.jsx

│   │   ├── Pagination.jsx

│   │   ├── ProtectedRoute.jsx

│   │   ├── SearchBar.jsx

│   │   └── StatusBadge.jsx

│   └── layout/

│       ├── DashboardLayout.jsx

│       ├── Sidebar.jsx

│       └── Topbar.jsx

│

└── pages/

├── AnalyticsPage.jsx

├── CustomersPage.jsx

├── DashboardPage.jsx

├── InventoryPage.jsx

├── LoginPage.jsx

├── NotFoundPage.jsx

├── ProductsPage.jsx

├── RegisterPage.jsx

├── ShipmentTrackingPage.jsx

├── ShipmentsPage.jsx

└── WarehousesPage.jsx

Total files: ~120 source files across backend + frontend + infrastructure
