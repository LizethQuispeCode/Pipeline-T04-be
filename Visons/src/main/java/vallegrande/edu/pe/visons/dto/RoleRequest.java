package vallegrande.edu.pe.visons.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleRequest {
    @NotBlank(message = "name es requerido")
    @Size(min = 2, max = 50, message = "name debe tener entre 2 y 50 caracteres")
    private String name;

    @Size(max = 255, message = "description debe tener como máximo 255 caracteres")
    private String description;
}