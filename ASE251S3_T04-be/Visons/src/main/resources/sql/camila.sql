-- AQUI ESTA TODO LO DEL SPRINT 4, LOS IJOINS,
-- INDICES Y LOS TRIGGERS IMPLEMENTADOS EN LA TABLA PROVEEDORES

-- =========================================
-- JOIN 1: INNER JOIN
-- Proveedores con sus compras realizadas
-- =========================================

SELECT
    pr.provider_id,
    pr.company_name AS proveedor,
    pu.order_code,
    pu.purchase_date,
    pu.total_amount,
    pu.status
FROM PROVIDERS pr
INNER JOIN PURCHASES pu
    ON pu.provider_id = pr.provider_id1;
GO


-- =========================================
-- JOIN 2: LEFT JOIN
-- Todos los proveedores y sus lotes
-- =========================================

SELECT
    pr.provider_id,
    pr.company_name AS proveedor,
    b.batch_code,
    p.name AS producto,
    b.available_quantity_kg
FROM PROVIDERS pr
LEFT JOIN BATCHES b
    ON b.provider_id = pr.provider_id
LEFT JOIN PRODUCTS p
    ON p.product_id = b.product_id;
GO


-- =========================================
-- JOIN 3: RIGHT JOIN
-- Proveedores, lotes y productos
-- =========================================

SELECT
    pr.company_name AS proveedor,
    b.batch_code,
    p.name AS producto,
    b.available_quantity_kg
FROM PROVIDERS pr
RIGHT JOIN BATCHES b
    ON pr.provider_id = b.provider_id
LEFT JOIN PRODUCTS p
    ON p.product_id = b.product_id;
GO


-- =========================================
-- INDICE 1: BUSQUEDA POR NOMBRE DE PROVEEDOR
-- =========================================

-- Consulta antes del indice
SELECT *
FROM PROVIDERS
WHERE company_name = 'AgroExport SAC';
GO

-- Crear indice
CREATE INDEX idx_providers_company_name
ON PROVIDERS(company_name);
GO

-- Consulta despues del indice
SELECT *
FROM PROVIDERS
WHERE company_name = 'AgroExport SAC';
GO


-- =========================================
-- INDICE 2: BUSQUEDA POR TIPO DE PRODUCTO
-- =========================================

-- Consulta antes del indice
SELECT *
FROM PROVIDERS
WHERE product_type = 'Mangos';
GO

-- Crear indice
CREATE INDEX idx_providers_product_type
ON PROVIDERS(product_type);
GO

-- Consulta despues del indice
SELECT *
FROM PROVIDERS
WHERE product_type = 'Mangos';
GO


-- =========================================
-- INDICE 3: INDICE COMPUESTO
-- =========================================

-- Consulta antes del indice
SELECT *
FROM PROVIDERS
WHERE is_active = 1
AND company_name LIKE 'A%';
GO

-- Crear indice
CREATE INDEX idx_providers_active_company
ON PROVIDERS(is_active, company_name);
GO

-- Consulta despues del indice
SELECT *
FROM PROVIDERS
WHERE is_active = 1
AND company_name LIKE 'A%';
GO



-- =========================================
-- TABLA DE BITACORA PARA PROVEEDORES
-- =========================================

CREATE TABLE PROVIDERS_AUDIT (
    audit_id INT IDENTITY(1,1) PRIMARY KEY,
    provider_id INT,
    action_type NVARCHAR(50),
    company_name NVARCHAR(200),
    action_date DATETIME DEFAULT GETDATE()
);
GO

-- =========================================
-- TRIGGER 1: AFTER INSERT
-- REGISTRA NUEVOS PROVEEDORES
-- =========================================

CREATE TRIGGER trg_provider_insert
ON PROVIDERS
AFTER INSERT
AS
BEGIN
    INSERT INTO PROVIDERS_AUDIT
    (
        provider_id,
        action_type,
        company_name,
        action_date
    )
    SELECT
        provider_id,
        'INSERT',
        company_name,
        GETDATE()
    FROM inserted;
END;
GO

-- Prueba de trigger 1
INSERT INTO PROVIDERS
(
    company_name,
    tax_id,
    product_type,
    contact_email,
    contact_phone,
    address,
    is_active
)
VALUES
(
    'Proveedor Trigger',
    '55555555555',
    'Prueba',
    'trigger@test.com',
    '999999999',
    'Lima',
    1
);
GO

--Verificacion

SELECT * FROM PROVIDERS_AUDIT;
GO


-- =========================================
-- TRIGGER 2: AFTER UPDATE
-- REGISTRA MODIFICACIONES
-- =========================================

CREATE TRIGGER trg_provider_update
ON PROVIDERS
AFTER UPDATE
AS
BEGIN
    INSERT INTO PROVIDERS_AUDIT
    (
        provider_id,
        action_type,
        company_name,
        action_date
    )
    SELECT
        provider_id,
        'UPDATE',
        company_name,
        GETDATE()
    FROM inserted;
END;
GO

--Probar triggers 2

UPDATE PROVIDERS
SET company_name = 'Proveedor Actualizado'
WHERE provider_id = 1;
GO

--Verificacion
SELECT * FROM PROVIDERS_AUDIT;
GO
