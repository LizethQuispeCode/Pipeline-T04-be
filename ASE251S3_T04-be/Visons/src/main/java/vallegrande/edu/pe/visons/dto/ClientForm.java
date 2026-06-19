package vallegrande.edu.pe.visons.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientForm {
    private Integer clientId;

    @NotBlank(message = "companyName es requerido")
    @Size(min = 3, max = 200, message = "companyName debe tener entre 3 y 200 caracteres")
    private String companyName;

    @NotBlank(message = "taxId es requerido")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "taxId debe contener solo números entre 8 y 20 dígitos")
    private String taxId;

    @NotBlank(message = "country es requerido")
    @Size(min = 2, max = 100, message = "country debe tener entre 2 y 100 caracteres")
    private String country;

    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "phone debe contener solo números y símbolos válidos")
    private String phone;

    @NotBlank(message = "address es requerida")
    @Size(min = 4, max = 255, message = "address debe tener entre 4 y 255 caracteres")
    private String address;

    @NotBlank(message = "email es requerido")
    @Email(message = "email debe tener un formato válido")
    @Size(max = 150, message = "email debe tener como máximo 150 caracteres")
    private String email;

    private String profileImageUrl;

    @NotNull(message = "creditLimit es requerido")
    @DecimalMin(value = "0.0", message = "creditLimit debe ser mayor o igual a 0")
    private BigDecimal creditLimit;

    @NotNull(message = "active es requerido")
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private LocalDateTime restoredAt;
}