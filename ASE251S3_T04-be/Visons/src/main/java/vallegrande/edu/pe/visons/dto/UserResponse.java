package vallegrande.edu.pe.visons.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class UserResponse {
    private Integer userId;
    private Integer userTypeId;
    private String userTypeName;
    private String username;
    private Boolean active;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private Integer workerId;
    private Integer clientId;
    private String displayName;
    private WorkerForm worker;
    private ClientForm client;
    private List<RoleResponse> roles;
}