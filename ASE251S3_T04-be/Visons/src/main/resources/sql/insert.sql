USE Visons;
GO

SET NOCOUNT ON;
GO

--------------------------------------------------
-- UBIGEO (10)
--------------------------------------------------
INSERT INTO UBIGEO (ubigeo_code, department, province, district)
VALUES
('150101','Lima','Lima','Cercado'),
('150102','Lima','Lima','Ate'),
('150103','Lima','Lima','Surco'),
('040101','Arequipa','Arequipa','Cercado'),
('080101','Cusco','Cusco','Cusco'),
('120101','Junín','Huancayo','Huancayo'),
('050101','Ayacucho','Huamanga','Ayacucho'),
('130101','La Libertad','Trujillo','Trujillo'),
('200101','Piura','Piura','Piura'),
('210101','Puno','Puno','Puno');
GO

--------------------------------------------------
-- USER_TYPES
--------------------------------------------------
INSERT INTO USER_TYPES (name)
VALUES ('ADMIN'),('EMPLOYEE'),('CLIENT');
GO

--------------------------------------------------
-- WORKERS (CORREGIDO - usa UBIGEO REAL)
--------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM WORKERS WHERE document_number = '70000001')
INSERT INTO WORKERS (first_name,last_name,phone,email,address,ubigeo_id,document_type,document_number,hire_date)
SELECT 'Luis','Perez','999111111','luis@empresa.com','Lima',
       (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code='150101'),'DNI','70000001',GETDATE();

IF NOT EXISTS (SELECT 1 FROM WORKERS WHERE document_number = '70000002')
INSERT INTO WORKERS (first_name,last_name,phone,email,address,ubigeo_id,document_type,document_number,hire_date)
SELECT 'Ana','Lopez','999111112','ana@empresa.com','Lima',
       (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code='150102'),'DNI','70000002',GETDATE();

IF NOT EXISTS (SELECT 1 FROM WORKERS WHERE document_number = '70000003')
INSERT INTO WORKERS (first_name,last_name,phone,email,address,ubigeo_id,document_type,document_number,hire_date)
SELECT 'Carlos','Diaz','999111113','carlos@empresa.com','Lima',
       (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code='150103'),'DNI','70000003',GETDATE();

IF NOT EXISTS (SELECT 1 FROM WORKERS WHERE document_number = '70000004')
INSERT INTO WORKERS (first_name,last_name,phone,email,address,ubigeo_id,document_type,document_number,hire_date)
SELECT 'Maria','Torres','999111114','maria@empresa.com','Arequipa',
       (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code='040101'),'DNI','70000004',GETDATE();

IF NOT EXISTS (SELECT 1 FROM WORKERS WHERE document_number = '70000005')
INSERT INTO WORKERS (first_name,last_name,phone,email,address,ubigeo_id,document_type,document_number,hire_date)
SELECT 'Jose','Ramos','999111115','jose@empresa.com','Cusco',
       (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code='080101'),'DNI','70000005',GETDATE();

IF NOT EXISTS (SELECT 1 FROM WORKERS WHERE document_number = '70000006')
INSERT INTO WORKERS (first_name,last_name,phone,email,address,ubigeo_id,document_type,document_number,hire_date)
SELECT 'Elena','Vega','999111116','elena@empresa.com','Junin',
       (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code='120101'),'DNI','70000006',GETDATE();

IF NOT EXISTS (SELECT 1 FROM WORKERS WHERE document_number = '70000007')
INSERT INTO WORKERS (first_name,last_name,phone,email,address,ubigeo_id,document_type,document_number,hire_date)
SELECT 'Pedro','Castro','999111117','pedro@empresa.com','Ayacucho',
       (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code='050101'),'DNI','70000007',GETDATE();

IF NOT EXISTS (SELECT 1 FROM WORKERS WHERE document_number = '70000008')
INSERT INTO WORKERS (first_name,last_name,phone,email,address,ubigeo_id,document_type,document_number,hire_date)
SELECT 'Lucia','Flores','999111118','lucia@empresa.com','Trujillo',
       (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code='130101'),'DNI','70000008',GETDATE();

IF NOT EXISTS (SELECT 1 FROM WORKERS WHERE document_number = '70000009')
INSERT INTO WORKERS (first_name,last_name,phone,email,address,ubigeo_id,document_type,document_number,hire_date)
SELECT 'Diego','Mendoza','999111119','diego@empresa.com','Piura',
       (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code='200101'),'DNI','70000009',GETDATE();

IF NOT EXISTS (SELECT 1 FROM WORKERS WHERE document_number = '70000010')
INSERT INTO WORKERS (first_name,last_name,phone,email,address,ubigeo_id,document_type,document_number,hire_date)
SELECT 'Sofia','Reyes','999111120','sofia@empresa.com','Puno',
       (SELECT ubigeo_id FROM UBIGEO WHERE ubigeo_code='210101'),'DNI','70000010',GETDATE();
GO

--------------------------------------------------
-- CLIENTS (SEED PARA LOGIN DE CLIENTE)
--------------------------------------------------
INSERT INTO CLIENTS (company_name, tax_id, country, phone, address, email, profile_image_url, credit_limit, is_active, created_at)
VALUES
('AgroExport S.A.C.', '20601234567', 'Perú', NULL, 'Av. Industrial 456, Ica', 'cliente@agroexport.pe', NULL, 25000.00, 1, GETDATE());

DECLARE @ClientId INT = SCOPE_IDENTITY();

--------------------------------------------------
-- USERS (CORREGIDO)
--------------------------------------------------
INSERT INTO USERS (username,password_hash,user_type_id,worker_id,client_id,is_active)
VALUES
('luis@empresa.com','70000001',
 (SELECT id FROM USER_TYPES WHERE name='ADMIN'),
 1,NULL,1),

('ana@empresa.com','70000002',
 (SELECT id FROM USER_TYPES WHERE name='EMPLOYEE'),
 2,NULL,1),

('carlos@empresa.com','70000003',
 (SELECT id FROM USER_TYPES WHERE name='EMPLOYEE'),
 3,NULL,1),

('maria@empresa.com','70000004',
 (SELECT id FROM USER_TYPES WHERE name='EMPLOYEE'),
 4,NULL,1),

('jose@empresa.com','70000005',
 (SELECT id FROM USER_TYPES WHERE name='EMPLOYEE'),
 5,NULL,1),

('elena@empresa.com','70000006',
 (SELECT id FROM USER_TYPES WHERE name='EMPLOYEE'),
 6,NULL,1),

('pedro@empresa.com','70000007',
 (SELECT id FROM USER_TYPES WHERE name='EMPLOYEE'),
 7,NULL,1),

('lucia@empresa.com','70000008',
 (SELECT id FROM USER_TYPES WHERE name='EMPLOYEE'),
 8,NULL,1),

('diego@empresa.com','70000009',
 (SELECT id FROM USER_TYPES WHERE name='EMPLOYEE'),
 9,NULL,1),

('sofia@empresa.com','70000010',
 (SELECT id FROM USER_TYPES WHERE name='EMPLOYEE'),
 10,NULL,1),

('cliente@agroexport.pe','20601234567',
 (SELECT id FROM USER_TYPES WHERE name='CLIENT'),
 NULL,@ClientId,1);
GO

--------------------------------------------------
-- ROLES
--------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM ROLES WHERE name = 'ADMIN')
INSERT INTO ROLES (name, description)
VALUES ('ADMIN', 'Administrador del sistema');

IF NOT EXISTS (SELECT 1 FROM ROLES WHERE name = 'EMPLOYEE')
INSERT INTO ROLES (name, description)
VALUES ('EMPLOYEE', 'Empleado del sistema');

IF NOT EXISTS (SELECT 1 FROM ROLES WHERE name = 'CLIENT')
INSERT INTO ROLES (name, description)
VALUES ('CLIENT', 'Cliente del sistema');
GO
--------------------------------------------------
-- USER_ROLES (CORREGIDO)
--------------------------------------------------
DELETE FROM USER_ROLES;

INSERT INTO USER_ROLES (user_id, role_id)
VALUES
((SELECT user_id FROM USERS WHERE username='luis@empresa.com'),
 (SELECT role_id FROM ROLES WHERE name='ADMIN')),

((SELECT user_id FROM USERS WHERE username='ana@empresa.com'),
 (SELECT role_id FROM ROLES WHERE name='EMPLOYEE')),

((SELECT user_id FROM USERS WHERE username='carlos@empresa.com'),
 (SELECT role_id FROM ROLES WHERE name='EMPLOYEE')),

((SELECT user_id FROM USERS WHERE username='maria@empresa.com'),
 (SELECT role_id FROM ROLES WHERE name='EMPLOYEE')),

((SELECT user_id FROM USERS WHERE username='jose@empresa.com'),
 (SELECT role_id FROM ROLES WHERE name='EMPLOYEE')),

((SELECT user_id FROM USERS WHERE username='elena@empresa.com'),
 (SELECT role_id FROM ROLES WHERE name='EMPLOYEE')),

((SELECT user_id FROM USERS WHERE username='pedro@empresa.com'),
 (SELECT role_id FROM ROLES WHERE name='EMPLOYEE')),

((SELECT user_id FROM USERS WHERE username='lucia@empresa.com'),
 (SELECT role_id FROM ROLES WHERE name='EMPLOYEE')),

((SELECT user_id FROM USERS WHERE username='diego@empresa.com'),
 (SELECT role_id FROM ROLES WHERE name='EMPLOYEE')),

((SELECT user_id FROM USERS WHERE username='sofia@empresa.com'),
 (SELECT role_id FROM ROLES WHERE name='EMPLOYEE')),

((SELECT user_id FROM USERS WHERE username='cliente@agroexport.pe'),
 (SELECT role_id FROM ROLES WHERE name='CLIENT'));
GO

-- =========================================
-- TABLA: CATEGORIES
-- Necesaria para relacionar los productos
-- =========================================

INSERT INTO CATEGORIES (name, description, is_active)
VALUES
('Frutas', 'Productos frutales frescos', 1),
('Verduras', 'Productos vegetales frescos', 1),
('Exportacion', 'Productos para exportacion', 1),
('Organicos', 'Productos organicos certificados', 1),
('Citricos', 'Frutas citricas', 1),
('Tropicales', 'Frutas tropicales', 1),
('Congelados', 'Productos congelados', 1),
('Premium', 'Productos premium seleccionados', 1),
('Procesados', 'Productos procesados', 1),
('Agroindustria', 'Productos agroindustriales', 1);

-- =========================================
-- TABLA: PROVIDERS
-- Necesaria para registrar compras
-- =========================================

INSERT INTO PROVIDERS (
    company_name,
    tax_id,
    product_type,
    is_active,
    created_at
)
VALUES
('AgroExport SAC', '20111111111', 'Mangos', 1, GETDATE()),
('Campos del Sur SAC', '20222222222', 'Palta', 1, GETDATE()),
('Fresh Fruits Peru', '20333333333', 'Uvas', 1, GETDATE()),
('Green Valley SAC', '20444444444', 'Citricos', 1, GETDATE()),
('Vision Agro SAC', '20555555555', 'Banano', 1, GETDATE()),
('Natural Foods SAC', '20666666666', 'Arandanos', 1, GETDATE()),
('Exportadora Norte', '20777777777', 'Limon', 1, GETDATE()),
('Peru Fresh Company', '20888888888', 'Papaya', 1, GETDATE()),
('BioCampos SAC', '20999999999', 'Organicos', 1, GETDATE()),
('Sun Fruits Peru', '20101010101', 'Piña', 1, GETDATE());

-- =========================================
-- TABLA MAESTRA: PRODUCTS
-- 10 inserts de productos
-- =========================================

INSERT INTO PRODUCTS (
    category_id,
    name,
    variety,
    caliber,
    unit_measure,
    box_weight_kg,
    is_own_production,
    is_active,
    created_at
)
VALUES
(1, 'Mango', 'Kent', 'Grande', 'KG', 10.50, 1, 1, GETDATE()),
(1, 'Palta', 'Hass', 'Mediano', 'KG', 8.00, 1, 1, GETDATE()),
(5, 'Limon', 'Tahiti', 'Pequeño', 'KG', 12.00, 0, 1, GETDATE()),
(6, 'Papaya', 'Maradol', 'Grande', 'KG', 15.00, 0, 1, GETDATE()),
(6, 'Piña', 'Golden', 'Grande', 'KG', 14.00, 1, 1, GETDATE()),
(1, 'Uva', 'Red Globe', 'Mediano', 'KG', 9.50, 1, 1, GETDATE()),
(4, 'Arandano', 'Bluecrop', 'Pequeño', 'KG', 5.00, 0, 1, GETDATE()),
(5, 'Naranja', 'Valencia', 'Grande', 'KG', 13.00, 1, 1, GETDATE()),
(2, 'Espinaca', 'Baby', 'Pequeño', 'KG', 4.00, 0, 1, GETDATE()),
(8, 'Fresa', 'Premium', 'Mediano', 'KG', 6.50, 1, 1, GETDATE());

-- =========================================
-- TABLA TRANSACCIONAL: PURCHASES
-- Registro de compras realizadas
-- =========================================

INSERT INTO PURCHASES (
    provider_id,
    order_code,
    total_amount,
    status,
    purchase_date
)
VALUES
(1, 'PUR-001', 2500.00, 'COMPLETED', GETDATE()),
(2, 'PUR-002', 1800.50, 'COMPLETED', GETDATE()),
(3, 'PUR-003', 3200.75, 'PENDING', GETDATE()),
(4, 'PUR-004', 4100.20, 'COMPLETED', GETDATE()),
(5, 'PUR-005', 950.00, 'PENDING', GETDATE()),
(6, 'PUR-006', 2750.40, 'COMPLETED', GETDATE()),
(7, 'PUR-007', 3890.00, 'COMPLETED', GETDATE()),
(8, 'PUR-008', 1450.90, 'PENDING', GETDATE()),
(9, 'PUR-009', 5200.00, 'COMPLETED', GETDATE()),
(10, 'PUR-010', 1999.99, 'COMPLETED', GETDATE());

-- =========================================
-- TABLA TRANSACCIONAL: PURCHASE_DETAILS
-- Detalle de productos comprados
-- =========================================

INSERT INTO PURCHASE_DETAILS (
    purchase_id,
    product_id,
    quantity_kg,
    unit_price
)
VALUES
(1, 1, 500.000, 5.0000),
(2, 2, 300.000, 6.0016),
(3, 3, 700.000, 4.5725),
(4, 4, 1000.000, 4.1002),
(5, 5, 200.000, 4.7500),
(6, 6, 450.000, 6.1120),
(7, 7, 800.000, 4.8625),
(8, 8, 250.000, 5.8036),
(9, 9, 1200.000, 4.3333),
(10, 10, 350.000, 5.7142);



--Consulta beneficiada Tabla Products--
SELECT *
FROM PRODUCTS
WHERE name = 'Mango';

--Consulta beneficiada Tabla PURCHASES--
SELECT *
FROM PURCHASES
WHERE provider_id = 3;
