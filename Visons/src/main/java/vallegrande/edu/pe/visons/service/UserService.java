package vallegrande.edu.pe.visons.service;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;
import vallegrande.edu.pe.visons.dto.AuthLoginRequest;
import vallegrande.edu.pe.visons.dto.ClientProfileUpdateRequest;
import vallegrande.edu.pe.visons.dto.RoleResponse;
import vallegrande.edu.pe.visons.dto.UserResponse;
import vallegrande.edu.pe.visons.dto.UserRoleRequest;
import vallegrande.edu.pe.visons.dto.UserUpsertRequest;

public interface UserService {

    List<UserResponse> findAll();

    Optional<UserResponse> findById(Integer id);

    UserResponse authenticate(AuthLoginRequest request);

    UserResponse currentSessionUser(HttpSession session);

    UserResponse updateCurrentClientProfile(HttpSession session, ClientProfileUpdateRequest request);

    String uploadProfileImage(HttpSession session, MultipartFile file);

    UserResponse save(UserUpsertRequest request);

    UserResponse update(Integer id, UserUpsertRequest request);

    UserResponse toggleStatus(Integer id);

    List<UserResponse> findByRoleId(Integer roleId);

    List<RoleResponse> findRolesByUserId(Integer id);

    UserResponse assignRole(Integer id, UserRoleRequest request);
}