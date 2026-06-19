package vallegrande.edu.pe.visons.service;

import vallegrande.edu.pe.visons.dto.ProductMovementRequestDTO;
import vallegrande.edu.pe.visons.dto.ProductMovementResponseDTO;

public interface ProductMovementService {

    ProductMovementResponseDTO registerMovement(ProductMovementRequestDTO request);
}