CREATE DATABASE Visons;
GO

USE Visons;
GO

CREATE TABLE CATEGORIES (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255) NULL,
    is_active BIT NOT NULL DEFAULT 1
);

CREATE TABLE PRODUCTS (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    category_id INT NOT NULL FOREIGN KEY REFERENCES CATEGORIES(category_id),
    name NVARCHAR(150) NOT NULL,
    variety NVARCHAR(100) NULL,
    caliber NVARCHAR(50) NULL,
    unit_measure NVARCHAR(20) NOT NULL DEFAULT 'KG',
    box_weight_kg DECIMAL(10,2) NULL,
    is_own_production BIT NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    deleted_at DATETIME NULL,
    restored_at DATETIME NULL
);

CREATE TABLE PROVIDERS (
    provider_id INT IDENTITY(1,1) PRIMARY KEY,
    company_name NVARCHAR(200) NOT NULL,
    tax_id NVARCHAR(20) NULL UNIQUE,
    product_type NVARCHAR(100) NULL,
    contact_email NVARCHAR(150) NULL,
    contact_phone NVARCHAR(20) NULL,
    address NVARCHAR(MAX) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_by NVARCHAR(100) NULL,
    created_at DATETIME NULL,
    updated_by NVARCHAR(100) NULL,
    updated_at DATETIME NULL,
    deleted_at DATETIME NULL,
    restored_at DATETIME NULL
);

CREATE TABLE CLIENTS (
    client_id INT IDENTITY(1,1) PRIMARY KEY,
    company_name NVARCHAR(200) NOT NULL,
    tax_id NVARCHAR(20) NOT NULL UNIQUE,
    country NVARCHAR(100) NULL,
    phone NVARCHAR(20) NULL,
    address NVARCHAR(MAX) NULL,
    email NVARCHAR(150) NULL,
    profile_image_url NVARCHAR(500) NULL,
    credit_limit DECIMAL(18,2) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    deleted_at DATETIME NULL,
    restored_at DATETIME NULL
);

CREATE TABLE UBIGEO (
    ubigeo_id INT IDENTITY(1,1) PRIMARY KEY,
    ubigeo_code CHAR(6) NOT NULL UNIQUE,
    department NVARCHAR(100) NOT NULL,
    province NVARCHAR(100) NOT NULL,
    district NVARCHAR(100) NOT NULL,
    is_active BIT NOT NULL DEFAULT 1
);

CREATE TABLE CLIENT_REQUESTS (
    request_id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NOT NULL,
    company_name NVARCHAR(200) NULL,
    tax_id NVARCHAR(20) NULL,
    country NVARCHAR(100) NULL,
    email NVARCHAR(150) NOT NULL,
    phone NVARCHAR(20) NULL,
    address NVARCHAR(MAX) NULL,
    ubigeo_id INT NULL FOREIGN KEY REFERENCES UBIGEO(ubigeo_id),
    status NVARCHAR(50) NOT NULL DEFAULT 'Pending',
    request_date DATETIME NOT NULL DEFAULT GETDATE(),
    reviewed_by INT NULL,
    comments NVARCHAR(MAX) NULL
);

CREATE TABLE BATCHES (
    batch_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL FOREIGN KEY REFERENCES PRODUCTS(product_id),
    provider_id INT NULL FOREIGN KEY REFERENCES PROVIDERS(provider_id),
    batch_code NVARCHAR(50) NOT NULL UNIQUE,
    harvest_date DATE NULL,
    available_quantity_kg DECIMAL(18,3) NULL,
    origin_farm NVARCHAR(200) NULL
);

CREATE TABLE CURRENT_INVENTORY (
    inventory_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL FOREIGN KEY REFERENCES PRODUCTS(product_id),
    total_stock_kg DECIMAL(18,3) NOT NULL DEFAULT 0,
    reserved_stock_kg DECIMAL(18,3) NOT NULL DEFAULT 0,
    available_stock_kg DECIMAL(18,3) NOT NULL DEFAULT 0
);

CREATE TABLE PURCHASES (
    purchase_id INT IDENTITY(1,1) PRIMARY KEY,
    provider_id INT NOT NULL FOREIGN KEY REFERENCES PROVIDERS(provider_id),
    order_code NVARCHAR(50) NULL UNIQUE,
    total_amount DECIMAL(18,2) NULL,
    status NVARCHAR(50) NULL,
    purchase_date DATE NOT NULL DEFAULT GETDATE()
);

CREATE TABLE PURCHASE_DETAILS (
    detail_id INT IDENTITY(1,1) PRIMARY KEY,
    purchase_id INT NOT NULL FOREIGN KEY REFERENCES PURCHASES(purchase_id),
    product_id INT NOT NULL FOREIGN KEY REFERENCES PRODUCTS(product_id),
    quantity_kg DECIMAL(18,3) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL
);

CREATE TABLE ORDERS (
    order_id INT IDENTITY(1,1) PRIMARY KEY,
    client_id INT NOT NULL FOREIGN KEY REFERENCES CLIENTS(client_id),
    order_code NVARCHAR(50) NULL UNIQUE,
    order_date DATE NOT NULL DEFAULT GETDATE(),
    incoterm CHAR(3) NULL,
    status NVARCHAR(50) NULL
);

CREATE TABLE ORDER_DETAILS (
    order_detail_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL FOREIGN KEY REFERENCES ORDERS(order_id),
    product_id INT NOT NULL FOREIGN KEY REFERENCES PRODUCTS(product_id),
    batch_id INT NULL FOREIGN KEY REFERENCES BATCHES(batch_id),
    quantity_kg DECIMAL(18,3) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL
);

--Nuevas tablas para la interfaz del ADMIN--

CREATE TABLE USER_TYPES (
    id INT IDENTITY PRIMARY KEY,
    name NVARCHAR(20) UNIQUE NOT NULL
);


CREATE TABLE WORKERS (
    worker_id INT IDENTITY PRIMARY KEY,
    first_name NVARCHAR(100),
    last_name NVARCHAR(100),
    phone NVARCHAR(20),
    email NVARCHAR(150),
    address NVARCHAR(MAX),
    ubigeo_id INT,
    document_type NVARCHAR(20),
    document_number NVARCHAR(20) UNIQUE,
    hire_date DATE,
    status NVARCHAR(20) DEFAULT 'ACTIVE',
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    deleted_at DATETIME NULL,
    restored_at DATETIME NULL,

    FOREIGN KEY (ubigeo_id) REFERENCES UBIGEO(ubigeo_id)
);
CREATE TABLE USERS (
    user_id INT IDENTITY PRIMARY KEY,
    username NVARCHAR(50) UNIQUE NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    user_type_id INT NOT NULL,
    worker_id INT NULL,
    client_id INT NULL,
    is_active BIT DEFAULT 1,
    last_login DATETIME NULL,
    created_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (worker_id) REFERENCES WORKERS(worker_id),
    FOREIGN KEY (client_id) REFERENCES CLIENTS(client_id),
    FOREIGN KEY (user_type_id) REFERENCES USER_TYPES(id)
);


CREATE TABLE ROLES (
    role_id INT IDENTITY PRIMARY KEY,
    name NVARCHAR(50) UNIQUE NOT NULL,
    description NVARCHAR(255)
);


CREATE TABLE USER_ROLES (
    user_id INT,
    role_id INT,
    PRIMARY KEY (user_id, role_id),

    FOREIGN KEY (user_id) REFERENCES USERS(user_id),
    FOREIGN KEY (role_id) REFERENCES ROLES(role_id)
);

INSERT INTO USER_TYPES (name)
SELECT 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM USER_TYPES WHERE name = 'ADMIN');

INSERT INTO USER_TYPES (name)
SELECT 'EMPLOYEE'
WHERE NOT EXISTS (SELECT 1 FROM USER_TYPES WHERE name = 'EMPLOYEE');

INSERT INTO USER_TYPES (name)
SELECT 'CLIENT'
WHERE NOT EXISTS (SELECT 1 FROM USER_TYPES WHERE name = 'CLIENT');

INSERT INTO ROLES (name, description)
SELECT 'ADMIN', 'Administrador del sistema'
WHERE NOT EXISTS (SELECT 1 FROM ROLES WHERE name = 'ADMIN');

INSERT INTO ROLES (name, description)
SELECT 'EMPLOYEE', 'Trabajador operativo'
WHERE NOT EXISTS (SELECT 1 FROM ROLES WHERE name = 'EMPLOYEE');

INSERT INTO ROLES (name, description)
SELECT 'CLIENT', 'Cliente de la plataforma'
WHERE NOT EXISTS (SELECT 1 FROM ROLES WHERE name = 'CLIENT');

CREATE TABLE AUDIT_LOG (
    log_id INT IDENTITY PRIMARY KEY,
    user_id INT,
    action NVARCHAR(100),
    table_name NVARCHAR(100),
    record_id INT,
    date DATETIME DEFAULT GETDATE(),
    details NVARCHAR(MAX),

    FOREIGN KEY (user_id) REFERENCES USERS(user_id)
);


--Indice Tabla Maestra: PRODUCTS--
CREATE INDEX idx_products_name
ON PRODUCTS(name);

CREATE INDEX idx_products_state
ON PRODUCTS(is_active);

CREATE INDEX idx_products_category_state
ON PRODUCTS(category_id, is_active);

--Indice Tabla Transaccional: PURCHASES--
CREATE INDEX idx_purchases_provider
ON PURCHASES(provider_id);

--Indices para consultas frecuentes de pedidos--
CREATE INDEX idx_orders_client
ON ORDERS(client_id);

CREATE INDEX idx_orders_client_date
ON ORDERS(client_id, order_date DESC);

CREATE INDEX idx_orders_status_date
ON ORDERS(status, order_date DESC);

CREATE INDEX idx_orders_date
ON ORDERS(order_date DESC);

CREATE INDEX idx_order_details_order
ON ORDER_DETAILS(order_id);

CREATE INDEX idx_order_details_product
ON ORDER_DETAILS(product_id);

--Indice para bandeja de solicitudes de clientes--
CREATE INDEX idx_client_requests_status_date
ON CLIENT_REQUESTS(status, request_date DESC);
