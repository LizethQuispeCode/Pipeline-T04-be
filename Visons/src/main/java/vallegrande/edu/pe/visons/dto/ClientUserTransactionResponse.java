package vallegrande.edu.pe.visons.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ClientUserTransactionResponse {

    private Integer clientId;
    private String companyName;
    private String taxId;
    private String country;
    private String phone;
    private String address;
    private String email;
    private BigDecimal creditLimit;
    private Boolean clientActive;
    private Integer userId;
    private String username;
    private String userTypeName;
    private Boolean userActive;
    private LocalDateTime createdAt;
    private String message;
}
