IF DB_ID(N'Visons') IS NULL
BEGIN
    EXEC(N'CREATE DATABASE [Visons]');
END;
GO

USE Visons;
GO


SET NOCOUNT ON;
GO


-- =========================================
-- DROP TABLES (orden inverso por claves foraneas)
-- =========================================
DROP TABLE IF EXISTS AUDIT_LOG;
DROP TABLE IF EXISTS USER_ROLES;
DROP TABLE IF EXISTS ROLES;
DROP TABLE IF EXISTS USERS;
DROP TABLE IF EXISTS WORKERS;
DROP TABLE IF EXISTS USER_TYPES;
DROP TABLE IF EXISTS ORDER_DETAILS;
DROP TABLE IF EXISTS ORDERS;
DROP TABLE IF EXISTS PURCHASE_DETAILS;
DROP TABLE IF EXISTS PURCHASES;
DROP TABLE IF EXISTS CURRENT_INVENTORY;
DROP TABLE IF EXISTS BATCHES;
DROP TABLE IF EXISTS CLIENT_REQUESTS;
DROP TABLE IF EXISTS UBIGEO;
DROP TABLE IF EXISTS CLIENTS;
DROP TABLE IF EXISTS PROVIDERS;
DROP TABLE IF EXISTS PRODUCTS;
DROP TABLE IF EXISTS CATEGORIES;


-- =========================================
-- CREATE TABLES
-- =========================================
CREATE TABLE CATEGORIES (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255) NULL,
    is_active BIT NOT NULL DEFAULT 1
);

CREATE TABLE PRODUCTS (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    category_id INT NOT NULL,
    name NVARCHAR(150) NOT NULL,
    variety NVARCHAR(100) NULL,
    caliber NVARCHAR(50) NULL,
    unit_measure NVARCHAR(20) NOT NULL DEFAULT N'KG',
    box_weight_kg DECIMAL(10,2) NULL,
    is_own_production BIT NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    deleted_at DATETIME NULL,
    restored_at DATETIME NULL,
    
    -- FK
    CONSTRAINT FK_PRODUCTS_CATEGORIES
        FOREIGN KEY (category_id) REFERENCES CATEGORIES(category_id),
    -- UNIQUE: no productos repetidos
    CONSTRAINT UQ_PRODUCTS_NAME UNIQUE (name),

    -- CHECK: no valores negativos en peso
    CONSTRAINT CK_PRODUCTS_BOX_WEIGHT
        CHECK (box_weight_kg IS NULL OR box_weight_kg >= 0)
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
    restored_at DATETIME NULL,

    -- CHECK: telefono debe tener minimo 7 caracteres si se ingresa
    CONSTRAINT CK_PROVIDERS_PHONE
        CHECK (contact_phone IS NULL OR LEN(contact_phone) >= 7),

    -- CHECK: is_active solo permite 0 o 1
    CONSTRAINT CK_PROVIDERS_IS_ACTIVE
        CHECK (is_active IN (0, 1)),

    -- CHECK: tax_id debe tener minimo 8 digitos si se ingresa
    CONSTRAINT CK_PROVIDERS_TAX_ID
        CHECK (tax_id IS NULL OR LEN(tax_id) >= 8),

    -- CHECK: company_name no puede ser solo espacios en blanco
    CONSTRAINT CK_PROVIDERS_COMPANY_NAME
        CHECK (LEN(LTRIM(RTRIM(company_name))) >= 3)
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
    ubigeo_id INT NULL,
    status NVARCHAR(50) NOT NULL DEFAULT N'Pending',
    request_date DATETIME NOT NULL DEFAULT GETDATE(),
    reviewed_by INT NULL,
    comments NVARCHAR(MAX) NULL,
    CONSTRAINT FK_CLIENT_REQUESTS_UBIGEO
        FOREIGN KEY (ubigeo_id) REFERENCES UBIGEO(ubigeo_id)
);

CREATE TABLE BATCHES (
    batch_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    provider_id INT NULL,
    batch_code NVARCHAR(50) NOT NULL UNIQUE,
    harvest_date DATE NULL,
    available_quantity_kg DECIMAL(18,3) NULL,
    origin_farm NVARCHAR(200) NULL,
    CONSTRAINT FK_BATCHES_PRODUCTS
        FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id),
    CONSTRAINT FK_BATCHES_PROVIDERS
        FOREIGN KEY (provider_id) REFERENCES PROVIDERS(provider_id)
);

CREATE TABLE CURRENT_INVENTORY (
    inventory_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    total_stock_kg DECIMAL(18,3) NOT NULL DEFAULT 0,
    reserved_stock_kg DECIMAL(18,3) NOT NULL DEFAULT 0,
    available_stock_kg DECIMAL(18,3) NOT NULL DEFAULT 0,
    CONSTRAINT FK_CURRENT_INVENTORY_PRODUCTS
        FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id)
);

CREATE TABLE PURCHASES (
    purchase_id INT IDENTITY(1,1) PRIMARY KEY,
    provider_id INT NOT NULL,
    order_code NVARCHAR(50) NULL UNIQUE,
    total_amount DECIMAL(18,4) NULL,
    status NVARCHAR(50) NULL,
    purchase_date DATE NOT NULL DEFAULT GETDATE(),
    notes NVARCHAR(500) NULL,
    created_at DATETIME NULL DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    CONSTRAINT FK_PURCHASES_PROVIDERS
        FOREIGN KEY (provider_id) REFERENCES PROVIDERS(provider_id)
);

CREATE TABLE PURCHASE_DETAILS (
    detail_id INT IDENTITY(1,1) PRIMARY KEY,
    purchase_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity_kg DECIMAL(18,3) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL,
    CONSTRAINT FK_PURCHASE_DETAILS_PURCHASES
        FOREIGN KEY (purchase_id) REFERENCES PURCHASES(purchase_id),
    CONSTRAINT FK_PURCHASE_DETAILS_PRODUCTS
        FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id)
);

CREATE TABLE ORDERS (
    order_id INT IDENTITY(1,1) PRIMARY KEY,
    client_id INT NOT NULL,
    order_code NVARCHAR(50) NULL UNIQUE,
    order_date DATE NOT NULL DEFAULT GETDATE(),
    incoterm CHAR(3) NULL,
    status NVARCHAR(50) NULL,
    CONSTRAINT CK_ORDERS_INCOTERM
        CHECK (incoterm IS NULL OR incoterm IN ('EXW', 'FOB', 'CIF')),
    CONSTRAINT FK_ORDERS_CLIENTS
        FOREIGN KEY (client_id) REFERENCES CLIENTS(client_id)
);

CREATE TABLE ORDER_DETAILS (
    order_detail_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    batch_id INT NULL,
    quantity_kg DECIMAL(18,3) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL,
    CONSTRAINT FK_ORDER_DETAILS_ORDERS
        FOREIGN KEY (order_id) REFERENCES ORDERS(order_id),
    CONSTRAINT FK_ORDER_DETAILS_PRODUCTS
        FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id),
    CONSTRAINT FK_ORDER_DETAILS_BATCHES
        FOREIGN KEY (batch_id) REFERENCES BATCHES(batch_id)
);

CREATE TABLE USER_TYPES (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE WORKERS (
    worker_id INT IDENTITY(1,1) PRIMARY KEY,
    first_name NVARCHAR(100) NULL,
    last_name NVARCHAR(100) NULL,
    phone NVARCHAR(20) NULL,
    email NVARCHAR(150) NULL,
    address NVARCHAR(MAX) NULL,
    ubigeo_id INT NULL,
    document_type NVARCHAR(20) NULL,
    document_number NVARCHAR(20) NULL UNIQUE,
    hire_date DATE NULL,
    status NVARCHAR(20) DEFAULT N'ACTIVE',
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    deleted_at DATETIME NULL,
    restored_at DATETIME NULL,
    CONSTRAINT FK_WORKERS_UBIGEO
        FOREIGN KEY (ubigeo_id) REFERENCES UBIGEO(ubigeo_id)
);

CREATE TABLE USERS (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    user_type_id INT NOT NULL,
    worker_id INT NULL,
    client_id INT NULL,
    is_active BIT DEFAULT 1,
    last_login DATETIME NULL,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_USERS_USER_TYPES
        FOREIGN KEY (user_type_id) REFERENCES USER_TYPES(id),
    CONSTRAINT FK_USERS_WORKERS
        FOREIGN KEY (worker_id) REFERENCES WORKERS(worker_id),
    CONSTRAINT FK_USERS_CLIENTS
        FOREIGN KEY (client_id) REFERENCES CLIENTS(client_id)
);

CREATE TABLE ROLES (
    role_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(255) NULL
);

CREATE TABLE USER_ROLES (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_USER_ROLES_USERS
        FOREIGN KEY (user_id) REFERENCES USERS(user_id),
    CONSTRAINT FK_USER_ROLES_ROLES
        FOREIGN KEY (role_id) REFERENCES ROLES(role_id)
);

CREATE TABLE AUDIT_LOG (
    log_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NULL,
    action NVARCHAR(100) NULL,
    table_name NVARCHAR(100) NULL,
    record_id INT NULL,
    date DATETIME DEFAULT GETDATE(),
    details NVARCHAR(MAX) NULL,
    CONSTRAINT FK_AUDIT_LOG_USERS
        FOREIGN KEY (user_id) REFERENCES USERS(user_id)
);


-- =========================================
-- INDICES
-- =========================================

CREATE INDEX idx_purchases_provider ON PURCHASES(provider_id);
CREATE INDEX idx_purchases_date ON PURCHASES(purchase_date DESC);
CREATE INDEX idx_purchases_status ON PURCHASES(status);
CREATE INDEX idx_purchases_provider_date ON PURCHASES(provider_id, purchase_date DESC);
CREATE INDEX idx_clients_company_name ON CLIENTS(company_name);
CREATE INDEX idx_orders_client ON ORDERS(client_id);
CREATE INDEX idx_orders_client_date ON ORDERS(client_id, order_date DESC);
CREATE INDEX idx_orders_status_date ON ORDERS(status, order_date DESC);
CREATE INDEX idx_orders_date ON ORDERS(order_date DESC);
CREATE INDEX idx_order_details_order ON ORDER_DETAILS(order_id);
CREATE INDEX idx_order_details_product ON ORDER_DETAILS(product_id);
CREATE INDEX idx_client_requests_status_date ON CLIENT_REQUESTS(status, request_date DESC);


-- =========================================
-- SEEDS: CATALOGOS BASE
-- =========================================
INSERT INTO USER_TYPES (name)
VALUES
(N'ADMIN'),
(N'WORKER'),
(N'CLIENT');

INSERT INTO ROLES (name, description)
VALUES
(N'ADMIN', N'Administrador del sistema'),
(N'EMPLOYEE', N'Trabajador operativo'),
(N'CLIENT', N'Cliente de la plataforma');


-- =========================================
-- SEEDS: UBIGEO
-- =========================================
INSERT INTO UBIGEO (ubigeo_code, department, province, district)
VALUES
('150101', N'Lima', N'Lima', N'Cercado'),
('150102', N'Lima', N'Lima', N'Ate'),
('150103', N'Lima', N'Lima', N'Surco'),
('040101', N'Arequipa', N'Arequipa', N'Cercado'),
('080101', N'Cusco', N'Cusco', N'Cusco'),
('120101', N'Junin', N'Huancayo', N'Huancayo'),
('050101', N'Ayacucho', N'Huamanga', N'Ayacucho'),
('130101', N'La Libertad', N'Trujillo', N'Trujillo'),
('200101', N'Piura', N'Piura', N'Piura'),
('210101', N'Puno', N'Puno', N'Puno');


-- =========================================
-- SEEDS: WORKERS Y USERS
-- Las claves iniciales son el DNI/RUC para facilitar login en desarrollo.
-- =========================================
INSERT INTO WORKERS (first_name, last_name, phone, email, address, ubigeo_id, document_type, document_number, hire_date, status, is_active, created_at)
VALUES
(N'Luis',   N'Perez',   N'999111111', N'luis@empresa.com',   N'Lima',      (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '150101'), N'DNI', N'70000001', GETDATE(), N'ACTIVE', 1, GETDATE()),
(N'Ana',    N'Lopez',   N'999111112', N'ana@empresa.com',    N'Lima',      (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '150102'), N'DNI', N'70000002', GETDATE(), N'ACTIVE', 1, GETDATE()),
(N'Carlos', N'Diaz',    N'999111113', N'carlos@empresa.com', N'Lima',      (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '150103'), N'DNI', N'70000003', GETDATE(), N'ACTIVE', 1, GETDATE()),
(N'Maria',  N'Torres',  N'999111114', N'maria@empresa.com',  N'Arequipa',  (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '040101'), N'DNI', N'70000004', GETDATE(), N'ACTIVE', 1, GETDATE()),
(N'Jose',   N'Ramos',   N'999111115', N'jose@empresa.com',   N'Cusco',     (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '080101'), N'DNI', N'70000005', GETDATE(), N'ACTIVE', 1, GETDATE()),
(N'Elena',  N'Vega',    N'999111116', N'elena@empresa.com',  N'Junin',     (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '120101'), N'DNI', N'70000006', GETDATE(), N'ACTIVE', 1, GETDATE()),
(N'Pedro',  N'Castro',  N'999111117', N'pedro@empresa.com',  N'Ayacucho',  (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '050101'), N'DNI', N'70000007', GETDATE(), N'ACTIVE', 1, GETDATE()),
(N'Lucia',  N'Flores',  N'999111118', N'lucia@empresa.com',  N'Trujillo',  (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '130101'), N'DNI', N'70000008', GETDATE(), N'ACTIVE', 1, GETDATE()),
(N'Diego',  N'Mendoza', N'999111119', N'diego@empresa.com',  N'Piura',     (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '200101'), N'DNI', N'70000009', GETDATE(), N'ACTIVE', 1, GETDATE()),
(N'Sofia',  N'Reyes',   N'999111120', N'sofia@empresa.com',  N'Puno',      (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '210101'), N'DNI', N'70000010', GETDATE(), N'ACTIVE', 1, GETDATE());


-- =========================================
-- SEEDS: CLIENTS
-- =========================================
INSERT INTO CLIENTS (company_name, tax_id, country, phone, address, email, profile_image_url, credit_limit, is_active, created_at)
VALUES
(N'AgroExport S.A.C.',        N'20601234567', N'Peru',           N'956123456', N'Av. Industrial 456, Ica',      N'cliente@agroexport.pe',     NULL, 25000.00, 1, GETDATE()),
(N'Frutas del Mundo S.A.',    N'20602345678', N'Espana',         N'956123457', N'Calle Comercio 12, Madrid',    N'contacto@frutasmundo.es',   NULL, 40000.00, 1, GETDATE()),
(N'Pacific Traders LLC',      N'20603456789', N'Estados Unidos', N'956123458', N'500 Fifth Ave, New York',      N'info@pacifictraders.com',   NULL, 60000.00, 1, GETDATE()),
(N'Fresh Europe GmbH',        N'20604567890', N'Alemania',       N'956123459', N'Berliner Str. 45, Berlin',     N'orders@fresheurope.de',     NULL, 35000.00, 1, GETDATE()),
(N'Asia Fruits Import Co.',   N'20605678901', N'Japon',          N'956123460', N'Shinjuku 3-1-1, Tokyo',        N'import@asiafruits.jp',      NULL, 50000.00, 1, GETDATE()),
(N'Exportadora Lima S.A.C.',  N'20606789012', N'Peru',           N'956123461', N'Jr. de la Union 780, Lima',    N'ventas@exportlima.pe',      NULL, 20000.00, 1, GETDATE()),
(N'South America Foods Ltd.', N'20607890123', N'Reino Unido',    N'956123462', N'10 Downing Market, London',    N'buy@safoods.co.uk',         NULL, 45000.00, 1, GETDATE()),
(N'Organic World B.V.',       N'20608901234', N'Paises Bajos',   N'956123463', N'Amstelplein 1, Amsterdam',     N'hello@organicworld.nl',     NULL, 30000.00, 1, GETDATE()),
(N'Andes Natural Export SAC', N'20609012345', N'Peru',           N'956123464', N'Av. Ejercito 301, Arequipa',   N'info@andesnatural.pe',      NULL, 18000.00, 1, GETDATE()),
(N'Green Globe Trading Co.',  N'20610123456', N'Canada',         N'956123465', N'1200 West Georgia, Vancouver', N'trade@greenglobe.ca',       NULL, 55000.00, 1, GETDATE());


DECLARE @ClientId INT = (SELECT client_id FROM CLIENTS WHERE tax_id = N'20601234567');

INSERT INTO USERS (username, password_hash, user_type_id, worker_id, client_id, is_active, created_at)
VALUES
(N'luis@empresa.com',      N'70000001',    (SELECT id FROM USER_TYPES WHERE name = N'ADMIN'),  1,  NULL,      1, GETDATE()),
(N'ana@empresa.com',       N'70000002',    (SELECT id FROM USER_TYPES WHERE name = N'WORKER'), 2,  NULL,      1, GETDATE()),
(N'carlos@empresa.com',    N'70000003',    (SELECT id FROM USER_TYPES WHERE name = N'WORKER'), 3,  NULL,      1, GETDATE()),
(N'maria@empresa.com',     N'70000004',    (SELECT id FROM USER_TYPES WHERE name = N'WORKER'), 4,  NULL,      1, GETDATE()),
(N'jose@empresa.com',      N'70000005',    (SELECT id FROM USER_TYPES WHERE name = N'WORKER'), 5,  NULL,      1, GETDATE()),
(N'elena@empresa.com',     N'70000006',    (SELECT id FROM USER_TYPES WHERE name = N'WORKER'), 6,  NULL,      1, GETDATE()),
(N'pedro@empresa.com',     N'70000007',    (SELECT id FROM USER_TYPES WHERE name = N'WORKER'), 7,  NULL,      1, GETDATE()),
(N'lucia@empresa.com',     N'70000008',    (SELECT id FROM USER_TYPES WHERE name = N'WORKER'), 8,  NULL,      1, GETDATE()),
(N'diego@empresa.com',     N'70000009',    (SELECT id FROM USER_TYPES WHERE name = N'WORKER'), 9,  NULL,      1, GETDATE()),
(N'sofia@empresa.com',     N'70000010',    (SELECT id FROM USER_TYPES WHERE name = N'WORKER'), 10, NULL,      1, GETDATE()),
(N'cliente@agroexport.pe', N'20601234567', (SELECT id FROM USER_TYPES WHERE name = N'CLIENT'), NULL, @ClientId, 1, GETDATE());


INSERT INTO USER_ROLES (user_id, role_id)
VALUES
((SELECT user_id FROM USERS WHERE username = N'luis@empresa.com'),      (SELECT role_id FROM ROLES WHERE name = N'ADMIN')),
((SELECT user_id FROM USERS WHERE username = N'ana@empresa.com'),       (SELECT role_id FROM ROLES WHERE name = N'EMPLOYEE')),
((SELECT user_id FROM USERS WHERE username = N'carlos@empresa.com'),    (SELECT role_id FROM ROLES WHERE name = N'EMPLOYEE')),
((SELECT user_id FROM USERS WHERE username = N'maria@empresa.com'),     (SELECT role_id FROM ROLES WHERE name = N'EMPLOYEE')),
((SELECT user_id FROM USERS WHERE username = N'jose@empresa.com'),      (SELECT role_id FROM ROLES WHERE name = N'EMPLOYEE')),
((SELECT user_id FROM USERS WHERE username = N'elena@empresa.com'),     (SELECT role_id FROM ROLES WHERE name = N'EMPLOYEE')),
((SELECT user_id FROM USERS WHERE username = N'pedro@empresa.com'),     (SELECT role_id FROM ROLES WHERE name = N'EMPLOYEE')),
((SELECT user_id FROM USERS WHERE username = N'lucia@empresa.com'),     (SELECT role_id FROM ROLES WHERE name = N'EMPLOYEE')),
((SELECT user_id FROM USERS WHERE username = N'diego@empresa.com'),     (SELECT role_id FROM ROLES WHERE name = N'EMPLOYEE')),
((SELECT user_id FROM USERS WHERE username = N'sofia@empresa.com'),     (SELECT role_id FROM ROLES WHERE name = N'EMPLOYEE')),
((SELECT user_id FROM USERS WHERE username = N'cliente@agroexport.pe'), (SELECT role_id FROM ROLES WHERE name = N'CLIENT'));


-- =========================================
-- SEEDS: CLIENT_REQUESTS
-- =========================================
INSERT INTO CLIENT_REQUESTS (username, first_name, last_name, company_name, tax_id, country, email, phone, address, ubigeo_id, status, request_date, reviewed_by, comments)
VALUES
(N'solicitud.norte', N'Valeria', N'Campos', N'Agro Norte SAC',      N'20700123456', N'Peru', N'valeria@agronorte.pe', N'987111222', N'Av. Grau 150, Piura',     (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '200101'), N'Pending',  GETDATE(), NULL, NULL),
(N'solicitud.sur',   N'Mateo',   N'Rojas',  N'Export Sur Andino',  N'20700123457', N'Peru', N'mateo@exportsur.pe',   N'987111223', N'Av. Ejercito 400, Cusco', (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '080101'), N'Approved', GETDATE(), 1,    N'Aprobado para pruebas'),
(N'solicitud.lima',  N'Camila',  N'Vega',   N'Comercial Lima SAC', N'20700123458', N'Peru', N'camila@comlima.pe',    N'987111224', N'Av. Larco 900, Lima',     (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code = '150103'), N'Rejected', GETDATE(), 1,    N'Documentacion incompleta');


-- =========================================
-- SEEDS: CATEGORIES, PROVIDERS, PRODUCTS
-- =========================================
INSERT INTO CATEGORIES (name, description, is_active)
VALUES
(N'Frutas',        N'Productos frutales frescos',       1),
(N'Verduras',      N'Productos vegetales frescos',      1),
(N'Exportacion',   N'Productos para exportacion',       1),
(N'Organicos',     N'Productos organicos certificados', 1),
(N'Citricos',      N'Frutas citricas',                  1),
(N'Tropicales',    N'Frutas tropicales',                1),
(N'Congelados',    N'Productos congelados',             1),
(N'Premium',       N'Productos premium seleccionados',  1),
(N'Procesados',    N'Productos procesados',             1),
(N'Agroindustria', N'Productos agroindustriales',       1);

INSERT INTO PROVIDERS (company_name, tax_id, product_type, contact_email, contact_phone, address, is_active, created_by, created_at)
VALUES
(N'AgroExport SAC',     N'20111111111', N'Mangos',    N'ventas@agroexport.pe',  N'988111111', N'Piura',        1, N'system', GETDATE()),
(N'Campos del Sur SAC', N'20222222222', N'Palta',     N'contacto@campossur.pe', N'988111112', N'Ica',          1, N'system', GETDATE()),
(N'Fresh Fruits Peru',  N'20333333333', N'Uvas',      N'info@freshfruits.pe',   N'988111113', N'Ica',          1, N'system', GETDATE()),
(N'Green Valley SAC',   N'20444444444', N'Citricos',  N'ventas@greenvalley.pe', N'988111114', N'Junin',        1, N'system', GETDATE()),
(N'Vision Agro SAC',    N'20555555555', N'Banano',    N'comercial@vision.pe',   N'988111115', N'San Martin',   1, N'system', GETDATE()),
(N'Natural Foods SAC',  N'20666666666', N'Arandanos', N'contacto@natural.pe',   N'988111116', N'La Libertad',  1, N'system', GETDATE()),
(N'Exportadora Norte',  N'20777777777', N'Limon',     N'ventas@norte.pe',       N'988111117', N'Piura',        1, N'system', GETDATE()),
(N'Peru Fresh Company', N'20888888888', N'Papaya',    N'info@perufresh.pe',     N'988111118', N'San Martin',   1, N'system', GETDATE()),
(N'BioCampos SAC',      N'20999999999', N'Organicos', N'ventas@biocampos.pe',   N'988111119', N'Ayacucho',     1, N'system', GETDATE()),
(N'Sun Fruits Peru',    N'20101010101', N'Pina',      N'ventas@sunfruits.pe',   N'988111120', N'Loreto',       1, N'system', GETDATE());

INSERT INTO PRODUCTS (category_id, name, variety, caliber, unit_measure, box_weight_kg, is_own_production, is_active, created_at)
VALUES
(1, N'Mango',    N'Kent',      N'Grande',  N'KG', 10.50, 1, 1, GETDATE()),
(1, N'Palta',    N'Hass',      N'Mediano', N'KG',  8.00, 1, 1, GETDATE()),
(5, N'Limon',    N'Tahiti',    N'Pequeno', N'KG', 12.00, 0, 1, GETDATE()),
(6, N'Papaya',   N'Maradol',   N'Grande',  N'KG', 15.00, 0, 1, GETDATE()),
(6, N'Pina',     N'Golden',    N'Grande',  N'KG', 14.00, 1, 1, GETDATE()),
(1, N'Uva',      N'Red Globe', N'Mediano', N'KG',  9.50, 1, 1, GETDATE()),
(4, N'Arandano', N'Bluecrop',  N'Pequeno', N'KG',  5.00, 0, 1, GETDATE()),
(5, N'Naranja',  N'Valencia',  N'Grande',  N'KG', 13.00, 1, 1, GETDATE()),
(2, N'Espinaca', N'Baby',      N'Pequeno', N'KG',  4.00, 0, 1, GETDATE()),
(8, N'Fresa',    N'Premium',   N'Mediano', N'KG',  6.50, 1, 1, GETDATE());


-- =========================================
-- SEEDS: COMPRAS E INVENTARIO
-- =========================================
INSERT INTO PURCHASES (provider_id, order_code, total_amount, status, purchase_date, notes, created_at)
VALUES
(1,  N'PUR-001', 2500.0000, N'Completed', GETDATE(), N'Compra inicial de mangos',    GETDATE()),
(2,  N'PUR-002', 1800.5000, N'Completed', GETDATE(), N'Compra de paltas Hass',       GETDATE()),
(3,  N'PUR-003', 3200.7500, N'Completed', GETDATE(), N'Compra de limones Tahiti',    GETDATE()),
(4,  N'PUR-004', 4100.2000, N'Completed', GETDATE(), N'Compra de papayas',           GETDATE()),
(5,  N'PUR-005',  950.0000, N'Completed', GETDATE(), N'Compra de pinas Golden',      GETDATE()),
(6,  N'PUR-006', 2750.4000, N'Completed', GETDATE(), N'Compra de uvas Red Globe',    GETDATE()),
(7,  N'PUR-007', 3890.0000, N'Completed', GETDATE(), N'Compra de arandanos',         GETDATE()),
(8,  N'PUR-008', 1450.9000, N'Completed', GETDATE(), N'Compra de naranjas Valencia', GETDATE()),
(9,  N'PUR-009', 5200.0000, N'Completed', GETDATE(), N'Compra de espinaca baby',     GETDATE()),
(10, N'PUR-010', 1999.9900, N'Completed', GETDATE(), N'Compra de fresas premium',    GETDATE());

INSERT INTO PURCHASE_DETAILS (purchase_id, product_id, quantity_kg, unit_price)
VALUES
(1,  1,  500.000, 5.0000),
(2,  2,  300.000, 6.0016),
(3,  3,  700.000, 4.5725),
(4,  4, 1000.000, 4.1002),
(5,  5,  200.000, 4.7500),
(6,  6,  450.000, 6.1120),
(7,  7,  800.000, 4.8625),
(8,  8,  250.000, 5.8036),
(9,  9, 1200.000, 4.3333),
(10, 10, 350.000, 5.7142);

INSERT INTO BATCHES (product_id, provider_id, batch_code, harvest_date, available_quantity_kg, origin_farm)
VALUES
(1,  1,  N'BATCH-001', '2026-03-01',  800.000, N'Fundo El Mango, Piura'),
(2,  2,  N'BATCH-002', '2026-03-05',  600.000, N'Fundo Hass, Ica'),
(3,  7,  N'BATCH-003', '2026-02-20', 1200.000, N'Fundo Citrus, Junin'),
(4,  8,  N'BATCH-004', '2026-02-28',  900.000, N'Fundo Papaya, San Martin'),
(5,  10, N'BATCH-005', '2026-03-10',  750.000, N'Fundo Pinero, Loreto'),
(6,  3,  N'BATCH-006', '2026-03-08',  500.000, N'Fundo Vina, Ica'),
(7,  6,  N'BATCH-007', '2026-03-12',  300.000, N'Fundo Blueberry, La Libertad'),
(8,  4,  N'BATCH-008', '2026-02-15', 1000.000, N'Fundo Naranjal, Piura'),
(9,  9,  N'BATCH-009', '2026-03-03',  450.000, N'Fundo Organico, Ayacucho'),
(10, 5,  N'BATCH-010', '2026-03-15',  350.000, N'Fundo Fresero, Arequipa');

INSERT INTO CURRENT_INVENTORY (product_id, total_stock_kg, reserved_stock_kg, available_stock_kg)
VALUES
(1,   800.000, 200.000, 600.000),
(2,   600.000, 150.000, 450.000),
(3,  1200.000, 300.000, 900.000),
(4,   900.000, 100.000, 800.000),
(5,   750.000, 250.000, 500.000),
(6,   500.000,  50.000, 450.000),
(7,   300.000,  80.000, 220.000),
(8,  1000.000, 400.000, 600.000),
(9,   450.000, 120.000, 330.000),
(10,  350.000,  90.000, 260.000);


-- =========================================
-- SEEDS: PEDIDOS Y DETALLES PARA MODULO PDF
-- =========================================
INSERT INTO ORDERS (client_id, order_code, order_date, incoterm, status)
VALUES
(1, N'ORD-001', CAST(GETDATE() AS DATE),       'FOB', N'Pending'),
(2, N'ORD-002', DATEADD(DAY, -2, GETDATE()),   'CIF', N'Processing'),
(3, N'ORD-003', DATEADD(DAY, -5, GETDATE()),   'EXW', N'Completed'),
(1, N'ORD-004', DATEADD(DAY, -10, GETDATE()),  'FOB', N'Cancelled');

INSERT INTO ORDER_DETAILS (order_id, product_id, batch_id, quantity_kg, unit_price)
VALUES
(1, 1, 1, 120.000, 6.5000),
(1, 2, 2,  80.000, 7.2000),
(2, 6, 6, 150.000, 5.9000),
(2, 8, 8, 100.000, 4.8500),
(3, 3, 3, 250.000, 4.3000),
(3, 7, 7,  90.000, 8.7500),
(4, 5, 5,  60.000, 5.4000);

-- =========================================
-- SEED: AUDITORIA
-- =========================================
INSERT INTO AUDIT_LOG (user_id, action, table_name, record_id, details)
VALUES
((SELECT user_id FROM USERS WHERE username = N'luis@empresa.com'), N'INIT_DB', N'ALL', NULL, N'Carga inicial del script completo.sql');


-- =========================================
-- CONSULTAS DE VERIFICACION
-- =========================================
SELECT COUNT(*) AS total_categories FROM CATEGORIES;
SELECT COUNT(*) AS total_clients FROM CLIENTS;
SELECT COUNT(*) AS total_users FROM USERS;
SELECT COUNT(*) AS total_orders FROM ORDERS;
SELECT COUNT(*) AS total_order_details FROM ORDER_DETAILS;
SELECT COUNT(*) AS total_purchase_details FROM PURCHASE_DETAILS;


-- =========================================
-- CONSULTAS DE VERIFICACION - TRANSACCION COMPRAS (MODULO PROVEEDOR)
-- =========================================

-- Ver todas las ordenes de compra con nombre del proveedor
SELECT
    p.purchase_id,
    pv.company_name AS proveedor,
    pv.tax_id       AS ruc,
    p.order_code    AS codigo_compra,
    p.purchase_date AS fecha_compra,
    p.status        AS estado,
    p.total_amount  AS total,
    p.notes         AS notas,
    p.created_at    AS fecha_registro
FROM PURCHASES p
JOIN PROVIDERS pv ON pv.provider_id = p.provider_id
ORDER BY p.purchase_id DESC;

-- Ver el detalle de cada orden de compra con nombre del producto
SELECT
    pd.detail_id,
    pd.purchase_id,
    p.order_code    AS codigo_compra,
    pr.name         AS producto,
    pd.quantity_kg  AS cantidad_kg,
    pd.unit_price   AS precio_unitario,
    (pd.quantity_kg * pd.unit_price) AS subtotal_linea
FROM PURCHASE_DETAILS pd
JOIN PURCHASES p  ON p.purchase_id  = pd.purchase_id
JOIN PRODUCTS  pr ON pr.product_id  = pd.product_id
ORDER BY pd.purchase_id DESC, pd.detail_id ASC;

-- Ver inventario actual (para confirmar que el stock subio tras una compra)
SELECT
    ci.inventory_id,
    pr.name              AS producto,
    ci.total_stock_kg    AS stock_total_kg,
    ci.reserved_stock_kg AS stock_reservado_kg,
    ci.available_stock_kg AS stock_disponible_kg
FROM CURRENT_INVENTORY ci
JOIN PRODUCTS pr ON pr.product_id = ci.product_id
ORDER BY ci.inventory_id ASC;
