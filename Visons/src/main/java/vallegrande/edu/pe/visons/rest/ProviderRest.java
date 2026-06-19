package vallegrande.edu.pe.visons.rest;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import vallegrande.edu.pe.visons.dto.ProviderSummaryDTO;
import vallegrande.edu.pe.visons.dto.PurchaseRequestDTO;
import vallegrande.edu.pe.visons.dto.PurchaseResponseDTO;
import vallegrande.edu.pe.visons.model.Provider;
import vallegrande.edu.pe.visons.service.PurchaseService;
import vallegrande.edu.pe.visons.service.ProviderService;

@RestController
@RequestMapping("/v1/api/provider")
@Tag(name = "Provider API", description = "API for Provider management")
public class ProviderRest {

    private final ProviderService providerService;
    private final PurchaseService purchaseService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ProviderRest(ProviderService providerService, PurchaseService purchaseService) {
        this.providerService = providerService;
        this.purchaseService = purchaseService;
    }

    @GetMapping({"", "/"})
    @Operation(summary = "Get All Providers", description = "Get All Providers")
    public List<Provider> findAll() {
        return providerService.findAll();
    }

    @GetMapping("/summary")
    @Operation(summary = "Get Provider Summary", description = "Get Provider Summary with purchase metrics")
    public List<ProviderSummaryDTO> findSummary() {
        List<Provider> providers = providerService.findAllIncludingInactive();

        Map<Integer, Long> totalPurchases = new LinkedHashMap<>();
        Map<Integer, Double> totalVolumeKg = new LinkedHashMap<>();
        Map<Integer, Double> totalAmount = new LinkedHashMap<>();
        Map<Integer, java.time.LocalDateTime> lastPurchaseDate = new LinkedHashMap<>();
        Map<Integer, List<String>> mainProducts = new LinkedHashMap<>();

        jdbcTemplate.query("SELECT provider_id, COUNT(*) AS total_purchases, SUM(COALESCE(total_amount, 0)) AS total_amount, MAX(CAST(purchase_date AS datetime)) AS last_purchase_date FROM PURCHASES GROUP BY provider_id",
                rs -> {
                    while (rs.next()) {
                        Integer providerId = rs.getInt("provider_id");
                        totalPurchases.put(providerId, rs.getLong("total_purchases"));
                        totalAmount.put(providerId, rs.getDouble("total_amount"));
                        java.sql.Timestamp ts = rs.getTimestamp("last_purchase_date");
                        if (ts != null) {
                            lastPurchaseDate.put(providerId, ts.toLocalDateTime());
                        }
                    }
                });

        jdbcTemplate.query("SELECT p.provider_id, SUM(COALESCE(pd.quantity_kg, 0)) AS total_volume_kg FROM PURCHASES p JOIN PURCHASE_DETAILS pd ON pd.purchase_id = p.purchase_id GROUP BY p.provider_id",
                rs -> {
                    while (rs.next()) {
                        totalVolumeKg.put(rs.getInt("provider_id"), rs.getDouble("total_volume_kg"));
                    }
                });

        for (Provider provider : providers) {
            List<String> products = jdbcTemplate.query(
                    "SELECT TOP 3 pr.name FROM PURCHASES p JOIN PURCHASE_DETAILS pd ON pd.purchase_id = p.purchase_id JOIN PRODUCTS pr ON pr.product_id = pd.product_id WHERE p.provider_id = ? GROUP BY pr.name ORDER BY SUM(pd.quantity_kg) DESC",
                    ps -> ps.setInt(1, provider.getProviderId()),
                    (rs, rowNum) -> rs.getString("name")
            );
            mainProducts.put(provider.getProviderId(), products);
        }

        return providers.stream().map(provider -> {
            List<String> providerMainProducts = mainProducts.getOrDefault(provider.getProviderId(), new ArrayList<>());
            ProviderSummaryDTO dto = new ProviderSummaryDTO();
            dto.setProviderId(provider.getProviderId());
            dto.setCompanyName(provider.getCompanyName());
            dto.setTaxId(provider.getTaxId());
            dto.setProductType(providerMainProducts.isEmpty() ? provider.getProductType() : String.join(", ", providerMainProducts));
            dto.setContactEmail(provider.getContactEmail());
            dto.setContactPhone(provider.getContactPhone());
            dto.setAddress(provider.getAddress());
            dto.setIsActive(provider.getIsActive());
            dto.setCreatedAt(provider.getCreatedAt());
            dto.setUpdatedAt(provider.getUpdatedAt());
            dto.setTotalPurchases(totalPurchases.getOrDefault(provider.getProviderId(), 0L));
            dto.setTotalVolumeKg(totalVolumeKg.getOrDefault(provider.getProviderId(), 0D));
            dto.setTotalAmount(totalAmount.getOrDefault(provider.getProviderId(), 0D));
            dto.setLastPurchaseDate(lastPurchaseDate.get(provider.getProviderId()));
            dto.setMainProducts(providerMainProducts);
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/state/{state}")
    @Operation(summary = "Get Provider By STATE", description = "Get Provider By STATE")
    public List<Provider> findByState(@PathVariable String state) {
        return providerService.findByState(state);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Provider By ID", description = "Get Provider By ID")
    public Optional<Provider> findById(@PathVariable Integer id) {
        return providerService.findById(id);
    }

    @GetMapping("/{providerId}/purchases")
    @Operation(
        summary = "Listar compras del proveedor",
        description = "Retorna todas las órdenes de compra registradas para un proveedor específico, con cabecera y detalle."
    )
    public List<PurchaseResponseDTO> findPurchasesByProvider(@PathVariable Integer providerId) {
        return purchaseService.findByProviderId(providerId);
    }

    @PostMapping("/{providerId}/purchases")
    @Operation(
        summary = "Registrar compra para proveedor",
        description = "Crea una orden de compra con su detalle para el proveedor indicado en una sola acción transaccional."
    )
    public ResponseEntity<PurchaseResponseDTO> registerPurchaseForProvider(
            @PathVariable Integer providerId,
            @Valid @RequestBody PurchaseRequestDTO request) {
        if (request.getProviderId() == null || !request.getProviderId().equals(providerId)) {
            request.setProviderId(providerId);
        }
        PurchaseResponseDTO response = purchaseService.registerPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/save")
    @Operation(summary = "Crear (POST) - (fecha-hora)", description = "Save Provider")
    public Provider save(@Valid @RequestBody Provider provider) {
        return providerService.save(provider);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Editar (PUT) - (fecha-hora)", description = "Update Provider")
    public Provider update(@PathVariable Integer id, @Valid @RequestBody Provider provider) {
        return providerService.update(id, provider);
    }
    @PatchMapping("/{id}")
    @Operation(summary = "Eliminar (lógico)    (PATCH) - (fecha-hora)", description = "Logical Delete Provider")
    public Provider delete(@PathVariable Integer id) {
        return providerService.delete(id);
    }

    @PatchMapping("/restore/{id}")
    @Operation(summary = "Restaurar (lógico) (PATCH) - (fecha-hora).", description = "Logical Restore Provider")
    public Provider restore(@PathVariable Integer id) {
        return providerService.restore(id);
    }
}
