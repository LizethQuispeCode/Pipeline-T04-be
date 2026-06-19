package vallegrande.edu.pe.visons.service;

import java.util.List;
import java.util.Optional;

import vallegrande.edu.pe.visons.dto.ClientRequestActionRequest;
import vallegrande.edu.pe.visons.dto.ClientRequestActionResponse;
import vallegrande.edu.pe.visons.dto.ClientRequestTransactionRequest;
import vallegrande.edu.pe.visons.dto.ClientRequestTransactionResponse;

public interface ClientRequestService {

    List<ClientRequestTransactionResponse> findAll();

    List<ClientRequestTransactionResponse> findByStatus(String status);

    Optional<ClientRequestTransactionResponse> findById(Integer id);

    ClientRequestTransactionResponse save(ClientRequestTransactionRequest request);

    ClientRequestActionResponse approveRequest(Integer id, ClientRequestActionRequest request);

    ClientRequestActionResponse rejectRequest(Integer id, ClientRequestActionRequest request);
}
