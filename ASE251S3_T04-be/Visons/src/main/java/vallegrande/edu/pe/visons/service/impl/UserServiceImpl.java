package vallegrande.edu.pe.visons.service.impl;

import java.math.BigDecimal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import vallegrande.edu.pe.visons.dto.AuthLoginRequest;
import vallegrande.edu.pe.visons.dto.ClientForm;
import vallegrande.edu.pe.visons.dto.ClientProfileUpdateRequest;
import vallegrande.edu.pe.visons.dto.RoleResponse;
import vallegrande.edu.pe.visons.dto.UserResponse;
import vallegrande.edu.pe.visons.dto.UserRoleRequest;
import vallegrande.edu.pe.visons.dto.UserUpsertRequest;
import vallegrande.edu.pe.visons.dto.WorkerForm;
import vallegrande.edu.pe.visons.model.Client;
import vallegrande.edu.pe.visons.model.Role;
import vallegrande.edu.pe.visons.model.UserAccount;
import vallegrande.edu.pe.visons.model.UserType;
import vallegrande.edu.pe.visons.model.Worker;
import vallegrande.edu.pe.visons.repository.ClientRepository;
import vallegrande.edu.pe.visons.repository.RoleRepository;
import vallegrande.edu.pe.visons.repository.UserAccountRepository;
import vallegrande.edu.pe.visons.repository.UserRoleRepository;
import vallegrande.edu.pe.visons.repository.UserTypeRepository;
import vallegrande.edu.pe.visons.repository.WorkerRepository;
import vallegrande.edu.pe.visons.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private static final String SESSION_USER_ID = "VISONS_CURRENT_USER_ID";

    private final UserAccountRepository userAccountRepository;
    private final UserTypeRepository userTypeRepository;
    private final WorkerRepository workerRepository;
    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Path profileImagesDir;

    public UserServiceImpl(UserAccountRepository userAccountRepository, UserTypeRepository userTypeRepository,
            WorkerRepository workerRepository, ClientRepository clientRepository, RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            org.springframework.core.env.Environment environment) {
        this.userAccountRepository = userAccountRepository;
        this.userTypeRepository = userTypeRepository;
        this.workerRepository = workerRepository;
        this.clientRepository = clientRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        String uploadDir = environment.getProperty("app.upload-dir", "uploads/profile-images");
        this.profileImagesDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public List<UserResponse> findAll() {
        return userAccountRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Optional<UserResponse> findById(Integer id) {
        return userAccountRepository.findById(id).map(this::toResponse);
    }

    @Transactional
    @Override
    public UserResponse save(UserUpsertRequest request) {
        validateCreateRequest(request);

        if (userAccountRepository.findByUsernameIgnoreCase(request.getUsername().trim()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        String normalizedType = resolveUserType(request);
        UserType userType = getOrCreateUserType(normalizedType);

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(request.getUsername().trim());
        userAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userAccount.setUserTypeId(userType.getId());
        userAccount.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
        userAccount.setCreatedAt(LocalDateTime.now());

        if (isWorkerBackedType(normalizedType)) {
            Worker worker = saveWorker(request.getWorker());
            userAccount.setWorkerId(worker.getWorkerId());
            userAccount.setClientId(null);
        } else if ("CLIENT".equals(normalizedType)) {
            Client client = saveClient(request.getClient());
            userAccount.setClientId(client.getClientId());
            userAccount.setWorkerId(null);
        } else {
            throw new RuntimeException("Unsupported user type: " + normalizedType);
        }

        UserAccount saved = userAccountRepository.save(userAccount);

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with ID: " + request.getRoleId()));
            userRoleRepository.assignRole(saved.getUserId(), role.getRoleId());
        }

        return toResponse(saved);
    }

    @Transactional
    @Override
    public UserResponse update(Integer id, UserUpsertRequest request) {
        UserAccount existing = userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            Optional<UserAccount> duplicate = userAccountRepository.findByUsernameIgnoreCase(request.getUsername().trim());
            if (duplicate.isPresent() && !duplicate.get().getUserId().equals(id)) {
                throw new RuntimeException("Username already exists");
            }
            existing.setUsername(request.getUsername().trim());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getActive() != null) {
            existing.setActive(request.getActive());
        }

        String normalizedType = request.getUserType();
        if (normalizedType != null && !normalizedType.isBlank()) {
            normalizedType = normalizedType.trim().toUpperCase();
            UserType userType = getOrCreateUserType(normalizedType);
            existing.setUserTypeId(userType.getId());

            if (isWorkerBackedType(normalizedType)) {
                Worker worker = updateOrCreateWorker(existing.getWorkerId(), request.getWorker());
                existing.setWorkerId(worker.getWorkerId());
                existing.setClientId(null);
            } else if ("CLIENT".equals(normalizedType)) {
                Client client = updateOrCreateClient(existing.getClientId(), request.getClient());
                existing.setClientId(client.getClientId());
                existing.setWorkerId(null);
            }
        }

        UserAccount saved = userAccountRepository.save(existing);

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with ID: " + request.getRoleId()));
            userRoleRepository.assignRole(saved.getUserId(), role.getRoleId());
        }

        return toResponse(saved);
    }

    @Transactional
    @Override
    public UserResponse toggleStatus(Integer id) {
        UserAccount existing = userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        existing.setActive(existing.getActive() == null || !existing.getActive());
        return toResponse(userAccountRepository.save(existing));
    }

    @Transactional
    @Override
    public UserResponse authenticate(AuthLoginRequest request) {
        validateLoginRequest(request);

        String username = request.getUsername().trim();
        UserAccount userAccount = userAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Credenciales inválidas"));

        if (Boolean.FALSE.equals(userAccount.getActive())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta está inactiva");
        }

        String candidatePassword = request.getPassword().trim();
        String roleCredential = resolveRoleCredential(userAccount);
        boolean matchesRoleCredential = matchesCredential(candidatePassword, roleCredential);
        boolean matchesStoredCredential = matchesCredential(candidatePassword, userAccount.getPasswordHash());

        if (!matchesRoleCredential && !matchesStoredCredential) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        userAccount.setLastLogin(LocalDateTime.now());
        userAccountRepository.save(userAccount);
        return toResponse(userAccount);
    }

    @Override
    public UserResponse currentSessionUser(HttpSession session) {
        return toResponse(getAuthenticatedUser(session));
    }

    @Transactional
    @Override
    public UserResponse updateCurrentClientProfile(HttpSession session, ClientProfileUpdateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        UserAccount userAccount = getAuthenticatedUser(session);
        if (userAccount.getClientId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only client accounts can update this profile");
        }

        Client client = clientRepository.findById(userAccount.getClientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        boolean clientChanged = false;
        if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
            client.setCompanyName(request.getCompanyName().trim());
            clientChanged = true;
        }
        if (request.getPhone() != null) {
            client.setPhone(request.getPhone().trim().isBlank() ? null : request.getPhone().trim());
            clientChanged = true;
        }
        if (request.getAddress() != null) {
            client.setAddress(request.getAddress().trim().isBlank() ? null : request.getAddress().trim());
            clientChanged = true;
        }
        if (request.getCountry() != null) {
            client.setCountry(request.getCountry().trim());
            clientChanged = true;
        }
        if (request.getProfileImageUrl() != null) {
            client.setProfileImageUrl(request.getProfileImageUrl().trim().isBlank() ? null : request.getProfileImageUrl().trim());
            clientChanged = true;
        }

        boolean passwordChanged = request.getNewPassword() != null && !request.getNewPassword().isBlank();
        if (passwordChanged) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is required");
            }
            if (request.getConfirmPassword() == null || !request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password confirmation does not match");
            }
            if (!matchesCurrentPassword(userAccount, request.getCurrentPassword().trim())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is invalid");
            }

            userAccount.setPasswordHash(passwordEncoder.encode(request.getNewPassword().trim()));
            userAccountRepository.save(userAccount);
        }

        if (clientChanged) {
            client.setUpdatedAt(LocalDateTime.now());
            clientRepository.save(client);
        }

        return toResponse(getAuthenticatedUser(session));
    }

    @Transactional
    @Override
    public String uploadProfileImage(HttpSession session, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }

        UserAccount userAccount = getAuthenticatedUser(session);
        if (userAccount.getClientId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only client accounts can update this profile");
        }

        Client client = clientRepository.findById(userAccount.getClientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        ensureUploadDirectory();

        String originalName = file.getOriginalFilename() == null ? "profile" : file.getOriginalFilename();
        String extension = extractExtension(originalName);
        String safeFileName = "client-" + client.getClientId() + "-" + System.currentTimeMillis() + extension;
        Path targetFile = profileImagesDir.resolve(safeFileName).normalize();

        try {
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store profile image", ex);
        }

        // Save as /uploads/<folder>/<file> so it maps to the resource handler
        String relativePath = "/uploads/" + profileImagesDir.getFileName() + "/" + safeFileName;
        client.setProfileImageUrl(relativePath);
        client.setUpdatedAt(LocalDateTime.now());
        clientRepository.save(client);
        return relativePath;
    }

    @Override
    public List<UserResponse> findByRoleId(Integer roleId) {
        List<Integer> userIds = userRoleRepository.findUserIdsByRoleId(roleId);
        if (userIds.isEmpty()) {
            return List.of();
        }
        return userAccountRepository.findAllById(userIds).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<RoleResponse> findRolesByUserId(Integer id) {
        List<Integer> roleIds = userRoleRepository.findRoleIdsByUserId(id);
        if (roleIds.isEmpty()) {
            return List.of();
        }

        return roleRepository.findAllById(roleIds).stream().map(this::toRoleResponse)
                .sorted(Comparator.comparing(RoleResponse::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserResponse assignRole(Integer id, UserRoleRequest request) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + request.getRoleId()));

        userRoleRepository.assignRole(user.getUserId(), role.getRoleId());
        return toResponse(user);
    }

    private void validateCreateRequest(UserUpsertRequest request) {
        if (request == null) {
            throw new RuntimeException("Request body is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }
        if (resolveUserType(request) == null) {
            throw new RuntimeException("User type is required");
        }

        validateUniqueNestedAccounts(null, request);
    }

    private void validateLoginRequest(AuthLoginRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
    }

    private UserAccount getAuthenticatedUser(HttpSession session) {
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is required");
        }

        Object sessionUserId = session.getAttribute(SESSION_USER_ID);
        if (!(sessionUserId instanceof Integer userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User session is not available");
        }

        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User session is not valid"));
    }

    private boolean matchesCurrentPassword(UserAccount userAccount, String currentPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            return false;
        }

        String roleCredential = resolveRoleCredential(userAccount);
        return matchesCredential(currentPassword, userAccount.getPasswordHash()) || matchesCredential(currentPassword, roleCredential);
    }

    private String resolveUserType(UserUpsertRequest request) {
        if (request.getUserType() != null && !request.getUserType().isBlank()) {
            return request.getUserType().trim().toUpperCase();
        }
        if (request.getWorker() != null) {
            return "WORKER";
        }
        if (request.getClient() != null) {
            return "CLIENT";
        }
        return null;
    }

    private UserType getOrCreateUserType(String typeName) {
        return userTypeRepository.findByName(typeName).orElseGet(() -> {
            UserType userType = new UserType();
            userType.setName(typeName);
            return userTypeRepository.save(userType);
        });
    }

    private boolean isWorkerBackedType(String typeName) {
        return "WORKER".equals(typeName) || "ADMIN".equals(typeName);
    }

    private Worker saveWorker(WorkerForm workerForm) {
        if (workerForm == null) {
            throw new RuntimeException("Worker information is required");
        }

        ensureUniqueWorker(null, workerForm);

        Worker worker = new Worker();
        worker.setFirstName(workerForm.getFirstName());
        worker.setLastName(workerForm.getLastName());
        worker.setPhone(workerForm.getPhone());
        worker.setEmail(workerForm.getEmail());
        worker.setAddress(workerForm.getAddress());
        worker.setUbigeoId(workerForm.getUbigeoId());
        worker.setDocumentType(workerForm.getDocumentType());
        worker.setDocumentNumber(workerForm.getDocumentNumber());
        worker.setHireDate(workerForm.getHireDate() != null ? workerForm.getHireDate() : LocalDate.now());
        worker.setStatus(workerForm.getStatus() != null ? workerForm.getStatus() : "ACTIVE");
        worker.setCreatedAt(LocalDateTime.now());
        worker.setUpdatedAt(LocalDateTime.now());
        worker.setIsActive(Boolean.TRUE);
        worker.setDeletedAt(null);
        worker.setRestoredAt(null);
        return workerRepository.save(worker);
    }

    private Client saveClient(ClientForm clientForm) {
        if (clientForm == null) {
            throw new RuntimeException("Client information is required");
        }

        ensureUniqueClient(null, clientForm);

        Client client = new Client();
        client.setCompanyName(clientForm.getCompanyName());
        client.setTaxId(clientForm.getTaxId());
        client.setCountry(clientForm.getCountry());
        client.setPhone(clientForm.getPhone());
        client.setAddress(clientForm.getAddress());
        client.setEmail(clientForm.getEmail());
        client.setProfileImageUrl(clientForm.getProfileImageUrl());
        client.setCreditLimit(clientForm.getCreditLimit() != null ? clientForm.getCreditLimit() : BigDecimal.ZERO);
        client.setActive(clientForm.getActive() == null ? Boolean.TRUE : clientForm.getActive());
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        return clientRepository.save(client);
    }

    private Worker updateOrCreateWorker(Integer workerId, WorkerForm workerForm) {
        if (workerForm == null) {
            if (workerId == null) {
                throw new RuntimeException("Worker information is required");
            }
            return workerRepository.findById(workerId)
                    .orElseThrow(() -> new RuntimeException("Worker not found with ID: " + workerId));
        }

        ensureUniqueWorker(workerId, workerForm);

        Worker worker = workerId != null ? workerRepository.findById(workerId).orElse(new Worker()) : new Worker();
        worker.setFirstName(workerForm.getFirstName());
        worker.setLastName(workerForm.getLastName());
        worker.setPhone(workerForm.getPhone());
        worker.setEmail(workerForm.getEmail());
        worker.setAddress(workerForm.getAddress());
        worker.setUbigeoId(workerForm.getUbigeoId());
        worker.setDocumentType(workerForm.getDocumentType());
        worker.setDocumentNumber(workerForm.getDocumentNumber());
        worker.setHireDate(workerForm.getHireDate() != null ? workerForm.getHireDate() : LocalDate.now());
        worker.setStatus(workerForm.getStatus() != null ? workerForm.getStatus() : "ACTIVE");
        worker.setUpdatedAt(LocalDateTime.now());
        if (worker.getCreatedAt() == null) {
            worker.setCreatedAt(LocalDateTime.now());
        }
        if (worker.getIsActive() == null) {
            worker.setIsActive(Boolean.TRUE);
        }
        return workerRepository.save(worker);
    }

    private Client updateOrCreateClient(Integer clientId, ClientForm clientForm) {
        if (clientForm == null) {
            if (clientId == null) {
                throw new RuntimeException("Client information is required");
            }
            return clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        }

        ensureUniqueClient(clientId, clientForm);

        Client client = clientId != null ? clientRepository.findById(clientId).orElse(new Client()) : new Client();
        client.setCompanyName(clientForm.getCompanyName());
        client.setTaxId(clientForm.getTaxId());
        client.setCountry(clientForm.getCountry());
        client.setPhone(clientForm.getPhone());
        client.setAddress(clientForm.getAddress());
        client.setEmail(clientForm.getEmail());
        client.setProfileImageUrl(clientForm.getProfileImageUrl());
        client.setCreditLimit(clientForm.getCreditLimit() != null ? clientForm.getCreditLimit() : BigDecimal.ZERO);
        client.setActive(clientForm.getActive() == null ? Boolean.TRUE : clientForm.getActive());
        client.setUpdatedAt(LocalDateTime.now());
        if (client.getCreatedAt() == null) {
            client.setCreatedAt(LocalDateTime.now());
        }
        return clientRepository.save(client);
    }

    private UserResponse toResponse(UserAccount userAccount) {
        UserResponse response = new UserResponse();
        response.setUserId(userAccount.getUserId());
        response.setUserTypeId(userAccount.getUserTypeId());
        response.setUsername(userAccount.getUsername());
        response.setActive(userAccount.getActive() == null || userAccount.getActive());
        response.setLastLogin(userAccount.getLastLogin());
        response.setCreatedAt(userAccount.getCreatedAt());
        response.setWorkerId(userAccount.getWorkerId());
        response.setClientId(userAccount.getClientId());

        userTypeRepository.findById(userAccount.getUserTypeId())
                .ifPresent(userType -> response.setUserTypeName(userType.getName()));

        if (userAccount.getWorkerId() != null) {
            workerRepository.findById(userAccount.getWorkerId()).ifPresent(worker -> response.setWorker(mapWorker(worker)));
            response.setDisplayName(buildWorkerName(userAccount.getWorkerId()));
        }

        if (userAccount.getClientId() != null) {
            clientRepository.findById(userAccount.getClientId()).ifPresent(client -> response.setClient(mapClient(client)));
            response.setDisplayName(buildClientName(userAccount.getClientId()));
        }

        response.setRoles(findRolesByUserId(userAccount.getUserId()));
        return response;
    }

    private String buildWorkerName(Integer workerId) {
        return workerRepository.findById(workerId)
                .map(worker -> {
                    String firstName = worker.getFirstName() == null ? "" : worker.getFirstName();
                    String lastName = worker.getLastName() == null ? "" : worker.getLastName();
                    return (firstName + " " + lastName).trim();
                }).orElse("Worker");
    }

    private String buildClientName(Integer clientId) {
        return clientRepository.findById(clientId)
                .map(client -> client.getCompanyName() == null ? "Client" : client.getCompanyName()).orElse("Client");
    }

    private WorkerForm mapWorker(Worker worker) {
        WorkerForm form = new WorkerForm();
        form.setWorkerId(worker.getWorkerId());
        form.setFirstName(worker.getFirstName());
        form.setLastName(worker.getLastName());
        form.setPhone(worker.getPhone());
        form.setEmail(worker.getEmail());
        form.setAddress(worker.getAddress());
        form.setUbigeoId(worker.getUbigeoId());
        form.setDocumentType(worker.getDocumentType());
        form.setDocumentNumber(worker.getDocumentNumber());
        form.setHireDate(worker.getHireDate());
        form.setStatus(worker.getStatus());
        form.setCreatedAt(worker.getCreatedAt());
        form.setUpdatedAt(worker.getUpdatedAt());
        return form;
    }

    private ClientForm mapClient(Client client) {
        ClientForm form = new ClientForm();
        form.setClientId(client.getClientId());
        form.setCompanyName(client.getCompanyName());
        form.setTaxId(client.getTaxId());
        form.setCountry(client.getCountry());
        form.setPhone(client.getPhone());
        form.setAddress(client.getAddress());
        form.setEmail(client.getEmail());
        form.setProfileImageUrl(client.getProfileImageUrl());
        form.setCreditLimit(client.getCreditLimit());
        form.setActive(client.getActive());
        form.setCreatedAt(client.getCreatedAt());
        form.setUpdatedAt(client.getUpdatedAt());
        form.setDeletedAt(client.getDeletedAt());
        form.setRestoredAt(client.getRestoredAt());
        return form;
    }

    private RoleResponse toRoleResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setRoleId(role.getRoleId());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        response.setUserCount(userRoleRepository.countUsersByRoleId(role.getRoleId()));
        return response;
    }

    private String resolveRoleCredential(UserAccount userAccount) {
        if (userAccount.getClientId() != null) {
            return clientRepository.findById(userAccount.getClientId())
                    .map(Client::getTaxId)
                    .orElse(null);
        }

        if (userAccount.getWorkerId() != null) {
            return workerRepository.findById(userAccount.getWorkerId())
                    .map(Worker::getDocumentNumber)
                    .orElse(null);
        }

        return null;
    }

    private boolean matchesCredential(String rawPassword, String storedValue) {
        if (rawPassword == null || storedValue == null || storedValue.isBlank()) {
            return false;
        }

        if (rawPassword.equals(storedValue)) {
            return true;
        }

        try {
            return passwordEncoder.matches(rawPassword, storedValue);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private void ensureUploadDirectory() {
        try {
            Files.createDirectories(profileImagesDir);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create upload directory", ex);
        }
    }

    private String extractExtension(String originalName) {
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == originalName.length() - 1) {
            return "";
        }

        String extension = originalName.substring(lastDot).toLowerCase();
        return extension.length() <= 10 ? extension : "";
    }

    private void validateUniqueNestedAccounts(Integer currentUserId, UserUpsertRequest request) {
        if (request == null) {
            return;
        }

        if (request.getWorker() != null) {
            ensureUniqueWorker(request.getWorker().getWorkerId(), request.getWorker());
        }

        if (request.getClient() != null) {
            ensureUniqueClient(request.getClient().getClientId(), request.getClient());
        }
    }

    private void ensureUniqueWorker(Integer currentWorkerId, WorkerForm workerForm) {
        if (workerForm == null) {
            return;
        }

        if (workerForm.getDocumentNumber() != null && !workerForm.getDocumentNumber().isBlank()) {
            workerRepository.findByDocumentNumber(workerForm.getDocumentNumber().trim())
                    .filter(existing -> currentWorkerId == null || !existing.getWorkerId().equals(currentWorkerId))
                    .ifPresent(existing -> { throw new RuntimeException("El DNI/documento del trabajador ya existe"); });
        }

        if (workerForm.getEmail() != null && !workerForm.getEmail().isBlank()) {
            workerRepository.findByEmailIgnoreCase(workerForm.getEmail().trim())
                    .filter(existing -> currentWorkerId == null || !existing.getWorkerId().equals(currentWorkerId))
                    .ifPresent(existing -> { throw new RuntimeException("El correo del trabajador ya existe"); });
        }
    }

    private void ensureUniqueClient(Integer currentClientId, ClientForm clientForm) {
        if (clientForm == null) {
            return;
        }

        if (clientForm.getTaxId() != null && !clientForm.getTaxId().isBlank()) {
            clientRepository.findByTaxIdIgnoreCase(clientForm.getTaxId().trim())
                    .filter(existing -> currentClientId == null || !existing.getClientId().equals(currentClientId))
                    .ifPresent(existing -> { throw new RuntimeException("El RUC/TAX ID del cliente ya existe"); });
        }

        if (clientForm.getEmail() != null && !clientForm.getEmail().isBlank()) {
            clientRepository.findByEmailIgnoreCase(clientForm.getEmail().trim())
                    .filter(existing -> currentClientId == null || !existing.getClientId().equals(currentClientId))
                    .ifPresent(existing -> { throw new RuntimeException("El correo del cliente ya existe"); });
        }
    }
}
