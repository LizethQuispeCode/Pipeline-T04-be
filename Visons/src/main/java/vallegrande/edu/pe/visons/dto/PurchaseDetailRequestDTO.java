package vallegrande.edu.pe.visons.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class PurchaseDetailRequestDTO {

    @NotNull(message = "productId es requerido")
    private Integer productId;

    @NotNull(message = "quantityKg es requerida")
    @DecimalMin(value = "0.001", message = "quantityKg debe ser mayor que 0")
    private BigDecimal quantityKg;

    @NotNull(message = "unitPrice es requerido")
    @DecimalMin(value = "0.0", message = "unitPrice debe ser mayor o igual a 0")
    private BigDecimal unitPrice;

    public PurchaseDetailRequestDTO() {
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
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
}
