package vallegrande.edu.pe.visons.dto;

import lombok.Data;

@Data
public class ClientRequestActionRequest {
    private Integer reviewedBy;
    private String comments;
}