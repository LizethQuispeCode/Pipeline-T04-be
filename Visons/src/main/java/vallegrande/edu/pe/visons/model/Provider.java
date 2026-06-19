package vallegrande.edu.pe.visons.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Data
@Table(name = "PROVIDERS")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provider_id")
    private Integer providerId;

    @NotBlank(message = "companyName es requerido")
    @Size(min = 3, max = 200, message = "companyName debe tener entre 3 y 200 caracteres")
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @NotBlank(message = "taxId es requerido")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "taxId debe contener solo números entre 8 y 20 dígitos")
    @Column(name = "tax_id", unique = true, length = 20)
    private String taxId;

    @NotBlank(message = "productType es requerido")
    @Size(min = 2, max = 100, message = "productType debe tener entre 2 y 100 caracteres")
    @Column(name = "product_type", length = 100)
    private String productType;

    @Email(message = "contactEmail debe tener un formato válido")
    @Size(max = 150, message = "contactEmail debe tener como máximo 150 caracteres")
    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "contactPhone debe contener solo números y símbolos válidos")
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Size(max = 255, message = "address debe tener como máximo 255 caracteres")
    @Column(name = "address")
    private String address;

    @NotNull(message = "isActive es requerido")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "restored_at")
    private LocalDateTime restoredAt;
}