package vallegrande.edu.pe.visons.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ClientRequestTransactionResponse {

    private Integer requestId;
    private String transactionCode;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String companyName;
    private String taxId;
    private String country;
    private String email;
    private String phone;
    private String address;
    private Integer ubigeoId;
    private String status;
    private LocalDateTime requestDate;
    private Integer reviewedBy;
    private String comments;
}
