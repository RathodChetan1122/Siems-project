-- =========================================================
-- SIEMS SEED DATA — V2
-- NOTE: User accounts are seeded by com.siems.config.DataSeeder at
-- application startup (uses the injected PasswordEncoder bean to
-- guarantee valid BCrypt hashes regardless of salt/version).
-- =========================================================

-- =====================
-- ROLES
-- =====================
INSERT INTO roles (role_name, description) VALUES
('ADMIN', 'Full system access'),
('IMPORT_MANAGER', 'Manages supplier relations, purchase shipments, and customs documentation for imports'),
('EXPORT_MANAGER', 'Manages customer relations, sales shipments, and invoicing for exports'),
('INVENTORY_MANAGER', 'Manages warehouse stock, inventory levels, and reorder alerts');

-- =====================
-- SUPPLIERS
-- =====================
INSERT INTO suppliers (name, country, contact_email, phone, rating, address) VALUES
('Global Textiles Ltd.', 'India', 'contact@globaltextiles.com', '+91-9876543210', 4.5, 'Plot 12, Industrial Area, Surat, India'),
('Shenzhen Electronics Co.', 'China', 'sales@szelectronics.cn', '+86-1234567890', 4.2, 'Bao An District, Shenzhen, China'),
('European Auto Parts GmbH', 'Germany', 'info@europarts.de', '+49-3098765432', 4.7, 'Industriestrasse 5, Stuttgart, Germany');

-- =====================
-- CUSTOMERS
-- =====================
INSERT INTO customers (name, billing_address, shipping_address, contact_email, phone, credit_terms) VALUES
('Alpha Retail Inc.', '500 Market St, New York, USA', '500 Market St, New York, USA', 'orders@alpharetail.com', '+1-2025550123', 'NET_30'),
('Euro Mart GmbH', 'Hauptstrasse 22, Berlin, Germany', 'Hauptstrasse 22, Berlin, Germany', 'procurement@euromart.de', '+49-3012345678', 'NET_45'),
('Pacific Traders Pte Ltd', '88 Marina Bay, Singapore', '88 Marina Bay, Singapore', 'buying@pacifictraders.sg', '+65-66123456', 'NET_30');

-- =====================
-- PRODUCTS
-- =====================
INSERT INTO products (sku, name, category, unit_of_measure, unit_price, supplier_id) VALUES
('TEX-COT-001', 'Cotton Fabric Roll', 'Textiles', 'METER', 4.50, 1),
('TEX-SLK-002', 'Silk Fabric Roll', 'Textiles', 'METER', 12.00, 1),
('ELEC-BT-002', 'Bluetooth Earphones', 'Electronics', 'PCS', 12.00, 2),
('ELEC-CAM-003', 'Security Camera Module', 'Electronics', 'PCS', 35.00, 2),
('AUTO-BRK-001', 'Brake Pad Set', 'Automotive', 'SET', 28.50, 3),
('AUTO-FLT-002', 'Oil Filter', 'Automotive', 'PCS', 6.75, 3);

-- =====================
-- WAREHOUSES
-- NOTE: manager_id values reference seeded users (inventory_manager = row 4),
-- which are created by DataSeeder BEFORE Flyway runs this migration is NOT
-- guaranteed -- so manager_id is left NULL here and can be set later via the
-- API. This avoids a foreign-key ordering dependency between Java seeding
-- and SQL migrations.
-- =====================
INSERT INTO warehouses (name, location, code, capacity, manager_id, is_active) VALUES
('Central Warehouse Hyderabad', 'Hyderabad, Telangana, India', 'WH-HYD-01', 50000, NULL, TRUE),
('Port Warehouse Mumbai', 'Mumbai, Maharashtra, India', 'WH-MUM-01', 30000, NULL, TRUE),
('European Distribution Center', 'Hamburg, Germany', 'WH-HAM-01', 40000, NULL, TRUE);

-- =====================
-- INVENTORY
-- =====================
INSERT INTO inventory (product_id, warehouse_id, quantity, reorder_threshold) VALUES
(1, 1, 5000, 500),   -- Cotton Fabric Roll @ Hyderabad
(2, 1, 1200, 200),   -- Silk Fabric Roll @ Hyderabad
(3, 2, 200, 50),     -- Bluetooth Earphones @ Mumbai
(4, 2, 80, 20),      -- Security Camera Module @ Mumbai
(5, 3, 600, 100),    -- Brake Pad Set @ Hamburg
(6, 3, 45, 50);      -- Oil Filter @ Hamburg (low stock)

-- =====================
-- SHIPMENT STATUS (lookup) — order matters for state machine
-- =====================
INSERT INTO shipment_status (status_name, description) VALUES
('PENDING',    'Shipment order created, awaiting warehouse processing'),
('PACKED',     'Goods packed and ready for dispatch'),
('DISPATCHED', 'Shipment handed over to carrier'),
('IN_TRANSIT', 'Shipment en route to destination'),
('AT_CUSTOMS', 'Shipment held at customs for clearance'),
('DELIVERED',  'Shipment delivered to customer'),
('CANCELLED',  'Shipment cancelled');

-- =====================
-- SAMPLE SHIPMENT (PENDING, demonstrates reserved inventory)
-- created_by is left NULL for the same ordering reason as warehouses.manager_id
-- =====================
INSERT INTO shipments (tracking_number, supplier_id, customer_id, warehouse_id, created_by, current_status_id, carrier, etd, eta) VALUES
('SIEMS-SHP-DEMO0001', 1, 1, 1, NULL, 1, 'Maersk Line', CURRENT_DATE, CURRENT_DATE + INTERVAL '20 days');

INSERT INTO shipment_items (shipment_id, product_id, quantity, unit_price) VALUES
(1, 1, 500, 4.50);

-- Reflect the reservation in inventory (500 units reserved from Cotton Fabric Roll @ Hyderabad)
UPDATE inventory SET quantity = quantity - 500 WHERE product_id = 1 AND warehouse_id = 1;

INSERT INTO shipment_status_history (shipment_id, status_id, changed_by, remarks, location) VALUES
(1, 1, NULL, 'Shipment created — inventory reserved', 'Warehouse');

-- =====================
-- LOW STOCK ALERT (matches Oil Filter @ Hamburg, qty 45 <= threshold 50)
-- =====================
INSERT INTO low_stock_alerts (inventory_id, quantity_at_alert, threshold_at_alert, resolved)
SELECT inventory_id, quantity, reorder_threshold, FALSE
FROM inventory
WHERE product_id = 6 AND warehouse_id = 3;
