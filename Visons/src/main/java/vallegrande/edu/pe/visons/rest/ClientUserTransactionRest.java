package vallegrande.edu.pe.visons.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import vallegrande.edu.pe.visons.dto.ClientUserTransactionRequest;
import vallegrande.edu.pe.visons.dto.ClientUserTransactionResponse;
import vallegrande.edu.pe.visons.service.ClientUserTransactionService;

@RestController
@RequestMapping("/v1/api/client-user-transactions")
@Tag(name = "Client User Transaction API", description = "Transaccional: crea cliente y usuario en una sola accion")
public class ClientUserTransactionRest {

    private final ClientUserTransactionService service;

    public ClientUserTransactionRest(ClientUserTransactionService service) {
        this.service = service;
    }

    @GetMapping({"", "/"})
    @Operation(summary = "Listar clientes con usuario", description = "Lista la cabecera CLIENTS con su usuario relacionado en USERS")
    public List<ClientUserTransactionResponse> findAll() {
        return service.findAll();
    }

    @PostMapping({"", "/"})
    @Operation(summary = "Crear cliente y usuario", description = "Inserta CLIENTS y USERS en una sola transaccion")
    public ClientUserTransactionResponse create(@Valid @RequestBody ClientUserTransactionRequest request) {
        return service.create(request);
    }
}
