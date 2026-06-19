package vallegrande.edu.pe.visons.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpsertRequest {
    @NotNull(message = "roleId es requerido")
    private Integer roleId;

    @NotBlank(message = "userType es requerido")
    private String userType;

    @NotBlank(message = "username es requerido")
    @Size(min = 3, max = 50, message = "username debe tener entre 3 y 50 caracteres")
    private String username;

    @Size(min = 6, max = 100, message = "password debe tener entre 6 y 100 caracteres")
    private String password;

    private Boolean active;

    @Valid
    private WorkerForm worker;

    @Valid
    private ClientForm client;
}