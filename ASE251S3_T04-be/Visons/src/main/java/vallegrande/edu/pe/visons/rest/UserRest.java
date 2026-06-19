package vallegrande.edu.pe.visons.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vallegrande.edu.pe.visons.dto.RoleResponse;
import vallegrande.edu.pe.visons.dto.UserResponse;
import vallegrande.edu.pe.visons.dto.UserRoleRequest;
import vallegrande.edu.pe.visons.dto.UserUpsertRequest;
import vallegrande.edu.pe.visons.service.UserService;

@RestController
@RequestMapping("/v1/api/users")
public class UserRest {

    private final UserService userService;

    @Autowired
    public UserRest(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"", "/"})
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<UserResponse> findById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    @PostMapping
    public UserResponse save(@Valid @RequestBody UserUpsertRequest request) {
        return userService.save(request);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Integer id, @Valid @RequestBody UserUpsertRequest request) {
        return userService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public UserResponse toggleStatus(@PathVariable Integer id) {
        return userService.toggleStatus(id);
    }

    @GetMapping("/{id}/roles")
    public List<RoleResponse> findRolesByUserId(@PathVariable Integer id) {
        return userService.findRolesByUserId(id);
    }

    @PostMapping("/{id}/roles")
    public UserResponse assignRole(@PathVariable Integer id, @Valid @RequestBody UserRoleRequest request) {
        return userService.assignRole(id, request);
    }

    @GetMapping("/role/{roleId}")
    public List<UserResponse> findByRoleId(@PathVariable Integer roleId) {
        return userService.findByRoleId(roleId);
    }
}