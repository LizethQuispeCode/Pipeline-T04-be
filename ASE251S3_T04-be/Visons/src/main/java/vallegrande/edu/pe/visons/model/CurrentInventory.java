package vallegrande.edu.pe.visons.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "CURRENT_INVENTORY")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CurrentInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer inventoryId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "total_stock_kg", nullable = false, precision = 18, scale = 3)
    private BigDecimal totalStockKg;

    @Column(name = "reserved_stock_kg", nullable = false, precision = 18, scale = 3)
    private BigDecimal reservedStockKg;

    @Column(name = "available_stock_kg", nullable = false, precision = 18, scale = 3)
    private BigDecimal availableStockKg;
}