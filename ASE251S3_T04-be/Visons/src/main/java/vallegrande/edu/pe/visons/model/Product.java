package vallegrande.edu.pe.visons.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Data
@Table(name = "PRODUCTS")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @NotNull(message = "categoryId es requerido")
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @NotBlank(message = "name es requerido")
    @Size(min = 3, max = 150, message = "name debe tener entre 3 y 150 caracteres")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 100, message = "variety debe tener como máximo 100 caracteres")
    @Column(name = "variety", length = 100)
    private String variety;

    @Size(max = 50, message = "caliber debe tener como máximo 50 caracteres")
    @Column(name = "caliber", length = 50)
    private String caliber;

    @NotBlank(message = "unitMeasure es requerido")
    @Size(min = 1, max = 20, message = "unitMeasure debe tener entre 1 y 20 caracteres")
    @Column(name = "unit_measure", nullable = false, length = 20)
    private String unitMeasure;

    @NotNull(message = "boxWeightKg es requerido")
    @DecimalMin(value = "0.0", message = "boxWeightKg debe ser mayor o igual a 0")
    @Column(name = "box_weight_kg", precision = 10, scale = 2)
    private BigDecimal boxWeightKg;

    @NotNull(message = "isOwnProduction es requerido")
    @Column(name = "is_own_production", nullable = false)
    private Boolean isOwnProduction;

    @NotNull(message = "isActive es requerido")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "restored_at")
    private LocalDateTime restoredAt;

    @Transient
    @NotNull(message = "initialStockKg es requerido")
    @DecimalMin(value = "0.0", message = "initialStockKg debe ser mayor o igual a 0")
    @Digits(integer = 10, fraction = 3, message = "initialStockKg tiene un formato inválido")
    private BigDecimal initialStockKg;

    @Transient
    private BigDecimal totalStockKg;

    @Transient
    private BigDecimal reservedStockKg;

    @Transient
    private BigDecimal availableStockKg;
}
