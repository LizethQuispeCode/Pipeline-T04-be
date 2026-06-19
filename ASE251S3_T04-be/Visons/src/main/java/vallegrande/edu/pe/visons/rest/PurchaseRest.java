package vallegrande.edu.pe.visons.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import vallegrande.edu.pe.visons.dto.PurchaseRequestDTO;
import vallegrande.edu.pe.visons.dto.PurchaseResponseDTO;
import vallegrande.edu.pe.visons.service.PurchaseService;

@RestController
@Validated
@RequestMapping("/v1/api/purchases")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Purchase API", description = "API para gestión de órdenes de compra a proveedores")
public class PurchaseRest {

    private final PurchaseService purchaseService;

    @Autowired
    public PurchaseRest(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    @Operation(
        summary = "Registrar orden de compra",
        description = "Crea una orden de compra con su detalle en una sola acción. " +
                      "Valida proveedor activo, productos activos y suma el stock al inventario."
    )
    public ResponseEntity<PurchaseResponseDTO> registerPurchase(
            @Valid @RequestBody PurchaseRequestDTO request) {
        PurchaseResponseDTO response = purchaseService.registerPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
        summary = "Listar todas las órdenes de compra",
        description = "Retorna todas las órdenes de compra ordenadas por fecha descendente, con su detalle."
    )
    public ResponseEntity<List<PurchaseResponseDTO>> findAll() {
        return ResponseEntity.ok(purchaseService.findAll());
    }

    @GetMapping("/provider/{providerId}")
    @Operation(
        summary = "Listar órdenes de compra por proveedor",
        description = "Retorna todas las órdenes de compra de un proveedor específico."
    )
    public ResponseEntity<List<PurchaseResponseDTO>> findByProvider(@PathVariable Integer providerId) {
        return ResponseEntity.ok(purchaseService.findByProviderId(providerId));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar orden de compra por ID",
        description = "Retorna una orden de compra específica con su detalle completo."
    )
    public ResponseEntity<PurchaseResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseService.findById(id));
    }
}
