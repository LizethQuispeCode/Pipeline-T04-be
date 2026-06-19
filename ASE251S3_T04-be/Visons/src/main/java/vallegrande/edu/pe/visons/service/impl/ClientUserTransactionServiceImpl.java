package vallegrande.edu.pe.visons.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vallegrande.edu.pe.visons.dto.ClientUserTransactionRequest;
import vallegrande.edu.pe.visons.dto.ClientUserTransactionResponse;
import vallegrande.edu.pe.visons.model.Client;
import vallegrande.edu.pe.visons.model.Role;
import vallegrande.edu.pe.visons.model.UserAccount;
import vallegrande.edu.pe.visons.model.UserType;
import vallegrande.edu.pe.visons.repository.ClientRepository;
import vallegrande.edu.pe.visons.repository.RoleRepository;
import vallegrande.edu.pe.visons.repository.UserAccountRepository;
import vallegrande.edu.pe.visons.repository.UserRoleRepository;
import vallegrande.edu.pe.visons.repository.UserTypeRepository;
import vallegrande.edu.pe.visons.service.ClientUserTransactionService;

@Service
public class ClientUserTransactionServiceImpl implements ClientUserTransactionService {

    private static final String CLIENT_TYPE = "CLIENT";
    private static final String CLIENT_ROLE = "CLIENT";

    private final ClientRepository clientRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserTypeRepository userTypeRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ClientUserTransactionServiceImpl(ClientRepository clientRepository,
            UserAccountRepository userAccountRepository,
            UserTypeRepository userTypeRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository) {
        this.clientRepository = clientRepository;
        this.userAccountRepository = userAccountRepository;
        this.userTypeRepository = userTypeRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public List<ClientUserTransactionResponse> findAll() {
        return userAccountRepository.findByClientIdIsNotNullOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClientUserTransactionResponse create(ClientUserTransactionRequest request) {
        validateUniqueData(request);

        LocalDateTime now = LocalDateTime.now();

        Client client = new Client();
        client.setCompanyName(request.getCompanyName().trim());
        client.setTaxId(request.getTaxId().trim());
        client.setCountry(request.getCountry().trim());
        client.setPhone(normalizeNullable(request.getPhone()));
        client.setAddress(request.getAddress().trim());
        client.setEmail(request.getEmail().trim());
        client.setCreditLimit(request.getCreditLimit());
        client.setActive(Boolean.TRUE);
        client.setCreatedAt(now);
        client.setUpdatedAt(now);
        Client savedClient = clientRepository.save(client);

        UserType userType = getOrCreateUserType(CLIENT_TYPE);
        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserTypeId(userType.getId());
        user.setWorkerId(null);
        user.setClientId(savedClient.getClientId());
        user.setActive(Boolean.TRUE);
        user.setCreatedAt(now);
        UserAccount savedUser = userAccountRepository.save(user);

        Role role = getOrCreateRole(CLIENT_ROLE);
        userRoleRepository.assignRole(savedUser.getUserId(), role.getRoleId());

        ClientUserTransactionResponse response = toResponse(savedUser, savedClient);
        response.setMessage("Cliente y usuario creados correctamente en una sola transaccion");
        return response;
    }

    private void validateUniqueData(ClientUserTransactionRequest request) {
        clientRepository.findByTaxIdIgnoreCase(request.getTaxId().trim())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente con ese taxId");
                });

        clientRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente con ese email");
                });

        userAccountRepository.findByUsernameIgnoreCase(request.getUsername().trim())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese username");
                });
    }

    private ClientUserTransactionResponse toResponse(UserAccount user) {
        Client client = clientRepository.findById(user.getClientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cliente relacionado no encontrado"));
        return toResponse(user, client);
    }

    private ClientUserTransactionResponse toResponse(UserAccount user, Client client) {
        ClientUserTransactionResponse response = new ClientUserTransactionResponse();
        response.setClientId(client.getClientId());
        response.setCompanyName(client.getCompanyName());
        response.setTaxId(client.getTaxId());
        response.setCountry(client.getCountry());
        response.setPhone(client.getPhone());
        response.setAddress(client.getAddress());
        response.setEmail(client.getEmail());
        response.setCreditLimit(client.getCreditLimit());
        response.setClientActive(client.getActive());
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setUserActive(user.getActive());
        response.setCreatedAt(user.getCreatedAt());
        userTypeRepository.findById(user.getUserTypeId())
                .ifPresent(userType -> response.setUserTypeName(userType.getName()));
        return response;
    }

    private UserType getOrCreateUserType(String name) {
        return userTypeRepository.findByName(name)
                .orElseGet(() -> {
                    UserType userType = new UserType();
                    userType.setName(name);
                    return userTypeRepository.save(userType);
                });
    }

    private Role getOrCreateRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription("Rol de cliente");
                    return roleRepository.save(role);
                });
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        return value.trim();
    }
}
