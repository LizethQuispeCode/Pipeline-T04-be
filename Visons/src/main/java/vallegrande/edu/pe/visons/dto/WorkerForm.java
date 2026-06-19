package vallegrande.edu.pe.visons.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkerForm {
    private Integer workerId;

    @NotBlank(message = "firstName es requerido")
    @Size(min = 2, max = 100, message = "firstName debe tener entre 2 y 100 caracteres")
    private String firstName;

    @NotBlank(message = "lastName es requerido")
    @Size(min = 2, max = 100, message = "lastName debe tener entre 2 y 100 caracteres")
    private String lastName;

    @Pattern(regexp = "^[0-9]{9}$", message = "phone debe contener exactamente 9 dígitos")
    private String phone;

    @NotBlank(message = "email es requerido")
    @Email(message = "email debe tener un formato válido")
    @Size(max = 150, message = "email debe tener como máximo 150 caracteres")
    private String email;

    @NotBlank(message = "address es requerida")
    @Size(min = 4, max = 255, message = "address debe tener entre 4 y 255 caracteres")
    private String address;

    private Integer ubigeoId;

    @NotBlank(message = "documentType es requerido")
    @Size(max = 20, message = "documentType debe tener como máximo 20 caracteres")
    private String documentType;

    @NotBlank(message = "documentNumber es requerido")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "documentNumber debe contener solo números entre 8 y 20 dígitos")
    private String documentNumber;

    private LocalDate hireDate;

    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}