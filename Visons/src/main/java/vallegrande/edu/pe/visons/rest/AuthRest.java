package vallegrande.edu.pe.visons.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vallegrande.edu.pe.visons.dto.AuthLoginRequest;
import vallegrande.edu.pe.visons.dto.FileUploadResponse;
import vallegrande.edu.pe.visons.dto.ClientProfileUpdateRequest;
import vallegrande.edu.pe.visons.dto.UserResponse;
import vallegrande.edu.pe.visons.service.UserService;

@RestController
@RequestMapping("/v1/api/auth")
public class AuthRest {

    private static final String SESSION_USER_ID = "VISONS_CURRENT_USER_ID";

    private final UserService userService;

    public AuthRest(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody AuthLoginRequest request, HttpSession session) {
        UserResponse response = userService.authenticate(request);
        session.setAttribute(SESSION_USER_ID, response.getUserId());
        return response;
    }

    @GetMapping("/me")
    public UserResponse currentUser(HttpSession session) {
        return userService.currentSessionUser(session);
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(@Valid @RequestBody ClientProfileUpdateRequest request, HttpSession session) {
        return userService.updateCurrentClientProfile(session, request);
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileUploadResponse uploadProfilePhoto(@RequestParam("file") MultipartFile file, HttpSession session) {
        String relativePath = userService.uploadProfileImage(session, file);
        String absoluteUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(relativePath)
                .toUriString();
        return new FileUploadResponse(absoluteUrl, relativePath);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}