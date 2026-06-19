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
@Table(name = "PURCHASE_DETAILS")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class PurchaseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Integer purchaseDetailId;

    @Column(name = "purchase_id", nullable = false)
    private Integer purchaseId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "quantity_kg", nullable = false, precision = 18, scale = 3)
    private BigDecimal quantityKg;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPrice;
}
