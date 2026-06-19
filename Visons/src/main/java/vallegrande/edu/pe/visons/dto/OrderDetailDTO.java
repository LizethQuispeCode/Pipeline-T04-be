package vallegrande.edu.pe.visons.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class OrderDetailDTO {

    @NotNull(message = "productId es requerido")
    private Integer productId;

    private String productName;

    @NotNull(message = "quantityKg es requerida")
    @DecimalMin(value = "0.001", message = "quantityKg debe ser mayor que 0")
    private BigDecimal quantityKg;

    @DecimalMin(value = "0.0", message = "unitPrice debe ser mayor o igual a 0")
    private BigDecimal unitPrice;

    private BigDecimal lineTotal;

    public OrderDetailDTO() {
    }

    public OrderDetailDTO(Integer productId, String productName, BigDecimal quantityKg, BigDecimal unitPrice, BigDecimal lineTotal) {
        this.productId = productId;
        this.productName = productName;
        this.quantityKg = quantityKg;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
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
}
