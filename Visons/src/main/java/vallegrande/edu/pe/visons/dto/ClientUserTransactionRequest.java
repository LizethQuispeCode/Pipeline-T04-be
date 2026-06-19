package vallegrande.edu.pe.visons.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientUserTransactionRequest {

    @NotBlank(message = "companyName es requerido")
    @Size(min = 3, max = 200, message = "companyName debe tener entre 3 y 200 caracteres")
    private String companyName;

    @NotBlank(message = "taxId es requerido")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "taxId debe contener solo numeros entre 8 y 20 digitos")
    private String taxId;

    @NotBlank(message = "country es requerido")
    @Size(min = 2, max = 100, message = "country debe tener entre 2 y 100 caracteres")
    private String country;

    @Pattern(regexp = "^\\s*$|^[0-9+()\\-\\s]{7,20}$", message = "phone debe contener solo numeros y simbolos validos")
    private String phone;

    @NotBlank(message = "address es requerido")
    @Size(min = 4, max = 255, message = "address debe tener entre 4 y 255 caracteres")
    private String address;

    @NotBlank(message = "email es requerido")
    @Email(message = "email debe tener un formato valido")
    @Size(max = 150, message = "email debe tener como maximo 150 caracteres")
    private String email;

    @NotNull(message = "creditLimit es requerido")
    @DecimalMin(value = "0.0", message = "creditLimit debe ser mayor o igual a 0")
    private BigDecimal creditLimit;

    @NotBlank(message = "username es requerido")
    @Size(min = 3, max = 50, message = "username debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "password es requerido")
    @Size(min = 6, max = 100, message = "password debe tener entre 6 y 100 caracteres")
    private String password;
}
