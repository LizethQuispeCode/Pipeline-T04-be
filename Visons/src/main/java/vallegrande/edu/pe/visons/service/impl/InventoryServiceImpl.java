package vallegrande.edu.pe.visons.service.impl;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vallegrande.edu.pe.visons.model.CurrentInventory;
import vallegrande.edu.pe.visons.repository.CurrentInventoryRepository;
import vallegrande.edu.pe.visons.service.InventoryService;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final CurrentInventoryRepository currentInventoryRepository;

    public InventoryServiceImpl(CurrentInventoryRepository currentInventoryRepository) {
        this.currentInventoryRepository = currentInventoryRepository;
    }

    @Override
    public void reserveOrderStock(Integer productId, BigDecimal quantityKg, String productName) {
        CurrentInventory inventory = loadInventory(productId, productName);
        BigDecimal normalizedQuantity = normalize(quantityKg);
        if (normalizedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantityKg debe ser mayor que 0");
        }

        BigDecimal totalStock = normalize(inventory.getTotalStockKg());
        BigDecimal reservedStock = normalize(inventory.getReservedStockKg());
        BigDecimal availableStock = normalize(inventory.getAvailableStockKg());

        if (availableStock.compareTo(normalizedQuantity) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente para el producto " + productLabel(productId, productName));
        }

        inventory.setTotalStockKg(totalStock);
        inventory.setReservedStockKg(reservedStock.add(normalizedQuantity));
        inventory.setAvailableStockKg(totalStock.subtract(inventory.getReservedStockKg()));
        currentInventoryRepository.save(inventory);
    }

    @Override
    public void consumeReservedOrderStock(Integer productId, BigDecimal quantityKg, String productName) {
        CurrentInventory inventory = loadInventory(productId, productName);
        BigDecimal normalizedQuantity = normalize(quantityKg);
        if (normalizedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantityKg debe ser mayor que 0");
        }

        BigDecimal totalStock = normalize(inventory.getTotalStockKg());
        BigDecimal reservedStock = normalize(inventory.getReservedStockKg());

        if (reservedStock.signum() == 0) {
            return;
        }

        if (reservedStock.compareTo(normalizedQuantity) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El stock reservado del producto " + productLabel(productId, productName) + " es inconsistente");
        }

        if (totalStock.compareTo(normalizedQuantity) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock total insuficiente para el producto " + productLabel(productId, productName));
        }

        BigDecimal newTotalStock = totalStock.subtract(normalizedQuantity);
        BigDecimal newReservedStock = reservedStock.subtract(normalizedQuantity);
        inventory.setTotalStockKg(newTotalStock);
        inventory.setReservedStockKg(newReservedStock);
        inventory.setAvailableStockKg(newTotalStock.subtract(newReservedStock));
        currentInventoryRepository.save(inventory);
    }

    @Override
    public void releaseOrderStock(Integer productId, BigDecimal quantityKg, String productName) {
        CurrentInventory inventory = loadInventory(productId, productName);
        BigDecimal normalizedQuantity = normalize(quantityKg);
        if (normalizedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantityKg debe ser mayor que 0");
        }

        BigDecimal totalStock = normalize(inventory.getTotalStockKg());
        BigDecimal reservedStock = normalize(inventory.getReservedStockKg());
        BigDecimal availableStock = normalize(inventory.getAvailableStockKg());

        if (reservedStock.compareTo(normalizedQuantity) >= 0) {
            BigDecimal newReservedStock = reservedStock.subtract(normalizedQuantity);
            inventory.setTotalStockKg(totalStock);
            inventory.setReservedStockKg(newReservedStock);
            inventory.setAvailableStockKg(totalStock.subtract(newReservedStock));
            currentInventoryRepository.save(inventory);
            return;
        }

        if (reservedStock.signum() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El stock reservado del producto " + productLabel(productId, productName) + " es inconsistente");
        }

        inventory.setTotalStockKg(totalStock.add(normalizedQuantity));
        inventory.setReservedStockKg(BigDecimal.ZERO);
        inventory.setAvailableStockKg(availableStock.add(normalizedQuantity));
        currentInventoryRepository.save(inventory);
    }

    private CurrentInventory loadInventory(Integer productId, String productName) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId es requerido");
        }

        return currentInventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe inventario para el producto " + productLabel(productId, productName)));
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String productLabel(Integer productId, String productName) {
        return productName == null || productName.isBlank() ? String.valueOf(productId) : productName;
    }
}