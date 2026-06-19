package vallegrande.edu.pe.visons.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import vallegrande.edu.pe.visons.dto.ClientRequestActionRequest;
import vallegrande.edu.pe.visons.dto.ClientRequestActionResponse;
import vallegrande.edu.pe.visons.dto.ClientRequestTransactionRequest;
import vallegrande.edu.pe.visons.dto.ClientRequestTransactionResponse;
import vallegrande.edu.pe.visons.service.ClientRequestService;

@RestController
@RequestMapping("/v1/api/client-request")
@Tag(name = "Client Request API", description = "API for Client Registration Requests")
public class ClientRequestRest {

    private final ClientRequestService clientRequestService;

    @Autowired
    public ClientRequestRest(ClientRequestService clientRequestService) {
        this.clientRequestService = clientRequestService;
    }

    @GetMapping({"", "/"})
    @Operation(summary = "Get All Client Requests", description = "Get All Client Requests")
    public List<ClientRequestTransactionResponse> findAll() {
        return clientRequestService.findAll();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get Client Requests By Status", description = "Get Client Requests By Status (Pending, Approved, Rejected)")
    public List<ClientRequestTransactionResponse> findByStatus(@PathVariable String status) {
        return clientRequestService.findByStatus(status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Client Request By ID", description = "Get Client Request By ID")
    public Optional<ClientRequestTransactionResponse> findById(@PathVariable Integer id) {
        return clientRequestService.findById(id);
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve Client Request", description = "Approve a client registration request and create the client and login user")
    public ClientRequestActionResponse approve(@PathVariable Integer id,
            @RequestBody(required = false) ClientRequestActionRequest request) {
        return clientRequestService.approveRequest(id, request);
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject Client Request", description = "Reject a client registration request without creating customer records")
    public ClientRequestActionResponse reject(@PathVariable Integer id,
            @RequestBody(required = false) ClientRequestActionRequest request) {
        return clientRequestService.rejectRequest(id, request);
    }

    @PostMapping("/save")
    @Operation(summary = "Submit Client Request (POST)", description = "Submit a new client registration request from the landing page")
    public ClientRequestTransactionResponse save(@Valid @RequestBody ClientRequestTransactionRequest request) {
        return clientRequestService.save(request);
    }
}
