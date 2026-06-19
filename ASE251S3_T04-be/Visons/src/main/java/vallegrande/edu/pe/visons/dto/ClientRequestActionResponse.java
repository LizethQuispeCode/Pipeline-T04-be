package vallegrande.edu.pe.visons.dto;

import lombok.Data;

@Data
public class ClientRequestActionResponse {
    private String message;
    private Integer requestId;
    private String status;
    private Integer clientId;
    private Integer userId;
    private String loginUsername;
}