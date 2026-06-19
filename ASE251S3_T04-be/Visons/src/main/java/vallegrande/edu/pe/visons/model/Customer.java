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
import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Data
@Table(name = "CLIENTS")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Integer clientId;

    @NotBlank(message = "companyName es requerido")
    @Size(min = 3, max = 200, message = "companyName debe tener entre 3 y 200 caracteres")
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @NotBlank(message = "taxId es requerido")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "taxId debe contener solo números entre 8 y 20 dígitos")
    @Column(name = "tax_id", nullable = false, length = 20, unique = true)
    private String taxId;

    @NotBlank(message = "country es requerido")
    @Size(min = 2, max = 100, message = "country debe tener entre 2 y 100 caracteres")
    @Column(name = "country", length = 100)
    private String country;

    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "phone debe contener solo números y símbolos válidos")
    @Column(name = "phone", length = 20)
    private String phone;

    @NotBlank(message = "address es requerida")
    @Size(min = 4, max = 255, message = "address debe tener entre 4 y 255 caracteres")
    @Column(name = "address", columnDefinition = "NVARCHAR(MAX)")
    private String address;

    @NotBlank(message = "email es requerido")
    @Email(message = "email debe tener un formato válido")
    @Size(max = 150, message = "email debe tener como máximo 150 caracteres")
    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @NotNull(message = "creditLimit es requerido")
    @DecimalMin(value = "0.0", message = "creditLimit debe ser mayor o igual a 0")
    @Column(name = "credit_limit", precision = 18, scale = 2)
    private BigDecimal creditLimit;

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
}