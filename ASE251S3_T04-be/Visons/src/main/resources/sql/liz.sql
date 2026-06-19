
USE Visons;
GO

--Liz--

-- 1 Productos con categoría e inventario--
SELECT
    p.product_id,
    p.name AS producto,
    c.name AS categoria,
    ci.total_stock_kg,
    ci.available_stock_kg
FROM PRODUCTS p
INNER JOIN CATEGORIES c ON c.category_id = p.category_id
INNER JOIN CURRENT_INVENTORY ci ON ci.product_id = p.product_id;


-- 2 Todos los productos y su posible proveedor de lotes--
SELECT
    p.product_id,
    p.name AS producto,
    b.batch_code,
    b.available_quantity_kg,
    pr.company_name AS proveedor
FROM PRODUCTS p
LEFT JOIN BATCHES b ON b.product_id = p.product_id
LEFT JOIN PROVIDERS pr ON pr.provider_id = b.provider_id;


-- 3 Proveedores y productos asociados, incluso si no tienen productos--
SELECT
    COALESCE(p.product_id, b.product_id) AS product_id,
    p.name AS producto,
    c.name AS categoria,
    b.batch_code,
    b.available_quantity_kg,
    pr.company_name AS proveedor,
    pr.product_type
FROM PRODUCTS p

-- RIGHT JOIN: incluye todos los lotes aunque no haya producto--
RIGHT JOIN BATCHES b ON p.product_id = b.product_id

-- RIGHT JOIN: incluye todos los proveedores aunque no tengan lotes--
RIGHT JOIN PROVIDERS pr ON pr.provider_id = b.provider_id

-- LEFT JOIN: categorías (si existe producto)--
LEFT JOIN CATEGORIES c ON c.category_id = p.category_id

-- FULL OUTER JOIN REAL: asegura incluir productos sin batch Y batches sin producto--
FULL OUTER JOIN PURCHASE_DETAILS pd ON pd.product_id = COALESCE(p.product_id, b.product_id);


-- Índice 1 Productos por Categoría y Estado--
CREATE INDEX idx_products_category_state
ON PRODUCTS(category_id, is_active);

SELECT
    product_id,
    name,
    category_id,
    is_active
FROM PRODUCTS
WHERE category_id = 1 AND is_active = 1;


-- Índice 2 Búsqueda de Productos por Nombre--
CREATE INDEX idx_products_name
ON PRODUCTS(name);

SELECT
    product_id,
    name,
    variety,
    caliber,
    unit_measure
FROM PRODUCTS
WHERE name = 'Mango';


-- Índice 3 Productos Activos del Sistema--
CREATE INDEX idx_products_state
ON PRODUCTS(is_active);

SELECT
    product_id,
    name,
    variety,
    unit_measure,
    is_active
FROM PRODUCTS
WHERE is_active = 1;


-- Trigger 1 Auditoría de modificaciones de productos--
GO
CREATE TRIGGER trg_products_after_update
ON PRODUCTS
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO AUDIT_LOG(user_id, action, table_name, record_id, date, details)
    SELECT
        NULL,
        'UPDATE',
        'PRODUCTS',
        i.product_id,
        GETDATE(),
        'Producto actualizado: ' + i.name
    FROM inserted i;
END;
GO

-- Verificación--
SELECT * FROM AUDIT_LOG ORDER BY log_id DESC;


-- Prueba de ejecución--
UPDATE PRODUCTS
SET variety = 'Kent Premium'
WHERE product_id = 1;


-- Trigger 2 Automatización de fecha de creación--
GO
CREATE TRIGGER trg_products_after_insert
ON PRODUCTS
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE p
    SET created_at = GETDATE()
    FROM PRODUCTS p
    INNER JOIN inserted i
        ON p.product_id = i.product_id;

    INSERT INTO AUDIT_LOG(user_id, action, table_name, record_id, date, details)
    SELECT
        NULL,
        'INSERT',
        'PRODUCTS',
        i.product_id,
        GETDATE(),
        'Nuevo producto registrado: ' + i.name
    FROM inserted i;
END;
GO


-- Verificación--
SELECT
    product_id,
    name,
    created_at
FROM PRODUCTS
WHERE name = 'Mandarina';


SELECT * FROM AUDIT_LOG ORDER BY log_id DESC;


-- Prueba de ejecución--
INSERT INTO PRODUCTS
(
    category_id,
    name,
    variety,
    caliber,
    unit_measure,
    box_weight_kg,
    is_own_production,
    is_active
)
VALUES
(1, 'Mandarina', 'Satsuma', 'Medium', 'kg', 5.0, 1, 1);