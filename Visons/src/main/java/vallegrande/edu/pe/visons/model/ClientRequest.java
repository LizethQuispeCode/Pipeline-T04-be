package vallegrande.edu.pe.visons.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Data
@Table(name = "CLIENT_REQUESTS")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ClientRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @NotBlank(message = "username es requerido")
    @Size(min = 3, max = 50, message = "username debe tener entre 3 y 50 caracteres")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "firstName es requerido")
    @Size(min = 2, max = 100, message = "firstName debe tener entre 2 y 100 caracteres")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "lastName es requerido")
    @Size(min = 2, max = 100, message = "lastName debe tener entre 2 y 100 caracteres")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(min = 3, max = 200, message = "companyName debe tener entre 3 y 200 caracteres")
    @Column(name = "company_name", length = 200)
    private String companyName;

    @Pattern(regexp = "^[0-9]{8,20}$", message = "taxId debe contener solo números entre 8 y 20 dígitos")
    @Column(name = "tax_id", length = 20)
    private String taxId;

    @Size(min = 2, max = 100, message = "country debe tener entre 2 y 100 caracteres")
    @Column(name = "country", length = 100)
    private String country;

    @NotBlank(message = "email es requerido")
    @Email(message = "email debe tener un formato válido")
    @Size(max = 150, message = "email debe tener como máximo 150 caracteres")
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "phone debe contener solo números y símbolos válidos")
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 255, message = "address debe tener como máximo 255 caracteres")
    @Column(name = "address", columnDefinition = "NVARCHAR(MAX)")
    private String address;

    @Column(name = "ubigeo_id")
    private Integer ubigeoId;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "reviewed_by")
    private Integer reviewedBy;

    @Column(name = "comments", columnDefinition = "NVARCHAR(MAX)")
    private String comments;

    @PrePersist
    public void prePersist() {
        if (status == null || status.isBlank()) {
            status = "Pending";
        }
        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }
    }
}
