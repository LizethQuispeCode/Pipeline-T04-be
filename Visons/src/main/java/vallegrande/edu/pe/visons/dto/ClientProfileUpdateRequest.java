package vallegrande.edu.pe.visons.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientProfileUpdateRequest {
    @NotBlank(message = "companyName es requerido")
    @Size(min = 3, max = 200, message = "companyName debe tener entre 3 y 200 caracteres")
    private String companyName;

    @NotBlank(message = "phone es requerido")
    @Size(min = 7, max = 20, message = "phone debe tener entre 7 y 20 caracteres")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "phone debe contener solo números y símbolos válidos")
    private String phone;

    @NotBlank(message = "address es requerida")
    @Size(min = 4, max = 255, message = "address debe tener entre 4 y 255 caracteres")
    private String address;

    @NotBlank(message = "country es requerido")
    @Size(min = 2, max = 100, message = "country debe tener entre 2 y 100 caracteres")
    private String country;

    private String profileImageUrl;

    @Size(min = 6, max = 100, message = "currentPassword debe tener entre 6 y 100 caracteres")
    private String currentPassword;

    @Size(min = 6, max = 100, message = "newPassword debe tener entre 6 y 100 caracteres")
    private String newPassword;

    @Size(min = 6, max = 100, message = "confirmPassword debe tener entre 6 y 100 caracteres")
    private String confirmPassword;
}
