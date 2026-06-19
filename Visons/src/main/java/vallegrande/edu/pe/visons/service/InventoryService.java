package vallegrande.edu.pe.visons.service;

import java.math.BigDecimal;

public interface InventoryService {

    void reserveOrderStock(Integer productId, BigDecimal quantityKg, String productName);

    void consumeReservedOrderStock(Integer productId, BigDecimal quantityKg, String productName);

    void releaseOrderStock(Integer productId, BigDecimal quantityKg, String productName);
}