package vallegrande.edu.pe.visons.service;

import java.util.List;

import vallegrande.edu.pe.visons.dto.ClientUserTransactionRequest;
import vallegrande.edu.pe.visons.dto.ClientUserTransactionResponse;

public interface ClientUserTransactionService {

    List<ClientUserTransactionResponse> findAll();

    ClientUserTransactionResponse create(ClientUserTransactionRequest request);
}
