package vallegrande.edu.pe.visons.service;

import java.util.List;

import vallegrande.edu.pe.visons.dto.PurchaseRequestDTO;
import vallegrande.edu.pe.visons.dto.PurchaseResponseDTO;

public interface PurchaseService {

    PurchaseResponseDTO registerPurchase(PurchaseRequestDTO request);

    List<PurchaseResponseDTO> findAll();

    List<PurchaseResponseDTO> findByProviderId(Integer providerId);

    PurchaseResponseDTO findById(Integer id);
}
