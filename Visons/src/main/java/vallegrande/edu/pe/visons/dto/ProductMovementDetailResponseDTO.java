package vallegrande.edu.pe.visons.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ProductMovementDetailResponseDTO {

    private Integer productId;
    private String productName;
    private BigDecimal quantityKg;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private BigDecimal availableStockAfter;

    public ProductMovementDetailResponseDTO() {
    }

    public ProductMovementDetailResponseDTO(Integer productId, String productName, BigDecimal quantityKg, BigDecimal unitPrice, BigDecimal lineTotal, BigDecimal availableStockAfter) {
        this.productId = productId;
        this.productName = productName;
        this.quantityKg = quantityKg;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.availableStockAfter = availableStockAfter;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(BigDecimal quantityKg) {
        this.quantityKg = quantityKg;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public BigDecimal getAvailableStockAfter() {
        return availableStockAfter;
    }

    public void setAvailableStockAfter(BigDecimal availableStockAfter) {
        this.availableStockAfter = availableStockAfter;
    }
}