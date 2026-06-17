-- =========================================================
-- SIEMS DATABASE SCHEMA — V1 Initial Schema
-- =========================================================

-- =====================
-- ROLES
-- =====================
CREATE TABLE roles (
    role_id     BIGSERIAL PRIMARY KEY,
    role_name   VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- =====================
-- USERS
-- =====================
CREATE TABLE users (
    user_id       BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id       BIGINT NOT NULL REFERENCES roles(role_id) ON DELETE RESTRICT,
    is_enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_role_id ON users(role_id);

-- =====================
-- REFRESH TOKENS
-- =====================
CREATE TABLE refresh_tokens (
    token_id    BIGSERIAL PRIMARY KEY,
    token       VARCHAR(512) NOT NULL UNIQUE,
    user_id     BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- =====================
-- SUPPLIERS
-- =====================
CREATE TABLE suppliers (
    supplier_id   BIGSERIAL PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    country       VARCHAR(80)  NOT NULL,
    contact_email VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    rating        NUMERIC(3,2) DEFAULT 0.00 CHECK (rating >= 0 AND rating <= 5),
    address       VARCHAR(255),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- CUSTOMERS
-- =====================
CREATE TABLE customers (
    customer_id      BIGSERIAL PRIMARY KEY,
    name             VARCHAR(150) NOT NULL,
    billing_address  VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    contact_email    VARCHAR(100) NOT NULL UNIQUE,
    phone            VARCHAR(20),
    credit_terms     VARCHAR(50) DEFAULT 'NET_30',
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- PRODUCTS
-- =====================
CREATE TABLE products (
    product_id      BIGSERIAL PRIMARY KEY,
    sku             VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    category        VARCHAR(80),
    unit_of_measure VARCHAR(20)  NOT NULL DEFAULT 'PCS',
    unit_price      NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    supplier_id     BIGINT REFERENCES suppliers(supplier_id) ON DELETE SET NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_products_supplier_id ON products(supplier_id);
CREATE INDEX idx_products_category ON products(category);

-- =====================
-- WAREHOUSES
-- =====================
CREATE TABLE warehouses (
    warehouse_id BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    location     VARCHAR(255) NOT NULL,
    code         VARCHAR(20)  NOT NULL UNIQUE,
    capacity     INTEGER,
    manager_id   BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================
-- INVENTORY
-- =====================
CREATE TABLE inventory (
    inventory_id      BIGSERIAL PRIMARY KEY,
    product_id        BIGINT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    warehouse_id      BIGINT NOT NULL REFERENCES warehouses(warehouse_id) ON DELETE CASCADE,
    quantity          INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    reorder_threshold INTEGER NOT NULL DEFAULT 10 CHECK (reorder_threshold >= 0),
    last_updated      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_product_warehouse UNIQUE (product_id, warehouse_id)
);
CREATE INDEX idx_inventory_product_id ON inventory(product_id);
CREATE INDEX idx_inventory_warehouse_id ON inventory(warehouse_id);
CREATE INDEX idx_inventory_low_stock ON inventory(product_id, warehouse_id)
    WHERE quantity <= reorder_threshold;

-- =====================
-- STOCK MOVEMENTS
-- =====================
CREATE TABLE stock_movements (
    movement_id     BIGSERIAL PRIMARY KEY,
    inventory_id    BIGINT NOT NULL REFERENCES inventory(inventory_id) ON DELETE CASCADE,
    movement_type   VARCHAR(20) NOT NULL CHECK (movement_type IN ('STOCK_IN','STOCK_OUT','ADJUSTMENT','TRANSFER_IN','TRANSFER_OUT')),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    reference_type  VARCHAR(30),
    reference_id    BIGINT,
    reason          VARCHAR(255),
    performed_by    BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_stock_movements_inventory_id ON stock_movements(inventory_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements(created_at DESC);

-- =====================
-- LOW STOCK ALERTS
-- =====================
CREATE TABLE low_stock_alerts (
    alert_id           BIGSERIAL PRIMARY KEY,
    inventory_id       BIGINT NOT NULL REFERENCES inventory(inventory_id) ON DELETE CASCADE,
    quantity_at_alert  INTEGER NOT NULL,
    threshold_at_alert INTEGER NOT NULL,
    resolved           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at        TIMESTAMP
);
CREATE INDEX idx_low_stock_alerts_inventory_id ON low_stock_alerts(inventory_id);
CREATE INDEX idx_low_stock_alerts_unresolved ON low_stock_alerts(inventory_id) WHERE resolved = FALSE;

-- =====================
-- SHIPMENT STATUS (lookup)
-- =====================
CREATE TABLE shipment_status (
    status_id   BIGSERIAL PRIMARY KEY,
    status_name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- =====================
-- SHIPMENTS
-- =====================
CREATE TABLE shipments (
    shipment_id       BIGSERIAL PRIMARY KEY,
    tracking_number   VARCHAR(50) NOT NULL UNIQUE,
    supplier_id       BIGINT NOT NULL REFERENCES suppliers(supplier_id) ON DELETE RESTRICT,
    customer_id       BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE RESTRICT,
    warehouse_id      BIGINT REFERENCES warehouses(warehouse_id) ON DELETE SET NULL,
    created_by        BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    current_status_id BIGINT NOT NULL REFERENCES shipment_status(status_id) ON DELETE RESTRICT,
    carrier           VARCHAR(100),
    etd               DATE,
    eta               DATE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_shipments_supplier_id ON shipments(supplier_id);
CREATE INDEX idx_shipments_customer_id ON shipments(customer_id);
CREATE INDEX idx_shipments_status_eta ON shipments(current_status_id, eta);

-- =====================
-- SHIPMENT STATUS HISTORY
-- =====================
CREATE TABLE shipment_status_history (
    history_id  BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    status_id   BIGINT NOT NULL REFERENCES shipment_status(status_id) ON DELETE RESTRICT,
    changed_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by  BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    remarks     TEXT,
    location    VARCHAR(150)
);
CREATE INDEX idx_ssh_shipment_id ON shipment_status_history(shipment_id);
CREATE INDEX idx_ssh_shipment_changed ON shipment_status_history(shipment_id, changed_at DESC);

-- =====================
-- SHIPMENT ITEMS
-- =====================
CREATE TABLE shipment_items (
    shipment_item_id BIGSERIAL PRIMARY KEY,
    shipment_id      BIGINT NOT NULL REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    product_id       BIGINT NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
    quantity         INTEGER NOT NULL CHECK (quantity > 0),
    unit_price       NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0)
);
CREATE INDEX idx_shipment_items_shipment_id ON shipment_items(shipment_id);
CREATE INDEX idx_shipment_items_product_id ON shipment_items(product_id);

-- =====================
-- NOTIFICATIONS
-- =====================
CREATE TABLE notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    title           VARCHAR(150) NOT NULL,
    message         TEXT NOT NULL,
    type            VARCHAR(30) NOT NULL DEFAULT 'INFO'
                     CHECK (type IN ('INFO','WARNING','ALERT','SUCCESS')),
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
