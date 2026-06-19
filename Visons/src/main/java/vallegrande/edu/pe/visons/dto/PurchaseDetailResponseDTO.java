package vallegrande.edu.pe.visons.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class PurchaseDetailResponseDTO {

    private Integer productId;
    private String productName;
    private BigDecimal quantityKg;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private BigDecimal totalStockAfter;

    public PurchaseDetailResponseDTO() {
    }

    public PurchaseDetailResponseDTO(Integer productId, String productName, BigDecimal quantityKg,
            BigDecimal unitPrice, BigDecimal lineTotal, BigDecimal totalStockAfter) {
        this.productId = productId;
        this.productName = productName;
        this.quantityKg = quantityKg;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.totalStockAfter = totalStockAfter;
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

    public BigDecimal getTotalStockAfter() {
        return totalStockAfter;
    }

    public void setTotalStockAfter(BigDecimal totalStockAfter) {
        this.totalStockAfter = totalStockAfter;
    }
}
