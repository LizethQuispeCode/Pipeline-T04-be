package vallegrande.edu.pe.visons.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleRequest {
    @NotNull(message = "roleId es requerido")
    private Integer roleId;
}