package vallegrande.edu.pe.visons.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vallegrande.edu.pe.visons.dto.PurchaseDetailRequestDTO;
import vallegrande.edu.pe.visons.dto.PurchaseDetailResponseDTO;
import vallegrande.edu.pe.visons.dto.PurchaseRequestDTO;
import vallegrande.edu.pe.visons.dto.PurchaseResponseDTO;
import vallegrande.edu.pe.visons.model.CurrentInventory;
import vallegrande.edu.pe.visons.model.Product;
import vallegrande.edu.pe.visons.model.Provider;
import vallegrande.edu.pe.visons.model.Purchase;
import vallegrande.edu.pe.visons.model.PurchaseDetail;
import vallegrande.edu.pe.visons.repository.CurrentInventoryRepository;
import vallegrande.edu.pe.visons.repository.ProductRepository;
import vallegrande.edu.pe.visons.repository.ProviderRepository;
import vallegrande.edu.pe.visons.repository.PurchaseDetailRepository;
import vallegrande.edu.pe.visons.repository.PurchaseRepository;
import vallegrande.edu.pe.visons.service.PurchaseService;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private static final String STATUS_COMPLETED = "Completed";

    private final PurchaseRepository purchaseRepository;
    private final PurchaseDetailRepository purchaseDetailRepository;
    private final ProviderRepository providerRepository;
    private final ProductRepository productRepository;
    private final CurrentInventoryRepository currentInventoryRepository;

    @Autowired
    public PurchaseServiceImpl(
            PurchaseRepository purchaseRepository,
            PurchaseDetailRepository purchaseDetailRepository,
            ProviderRepository providerRepository,
            ProductRepository productRepository,
            CurrentInventoryRepository currentInventoryRepository) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseDetailRepository = purchaseDetailRepository;
        this.providerRepository = providerRepository;
        this.productRepository = productRepository;
        this.currentInventoryRepository = currentInventoryRepository;
    }

    @Override
    @Transactional
    public PurchaseResponseDTO registerPurchase(PurchaseRequestDTO request) {
        validateRequest(request);

        // Validar proveedor activo
        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El proveedor con ID " + request.getProviderId() + " no existe"));

        if (!Boolean.TRUE.equals(provider.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El proveedor " + provider.getCompanyName() + " no está activo");
        }

        // Validar código único
        String purchaseCode = request.getPurchaseCode().trim();
        if (purchaseRepository.findByPurchaseCodeIgnoreCase(purchaseCode).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El código de compra '" + purchaseCode + "' ya existe");
        }

        LocalDate purchaseDate = request.getPurchaseDate() == null ? LocalDate.now() : request.getPurchaseDate();

        // Preparar líneas y calcular totales
        Map<Integer, Product> productCache = new HashMap<>();
        Map<Integer, CurrentInventory> inventoryCache = new HashMap<>();
        List<PreparedPurchaseLine> preparedLines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (PurchaseDetailRequestDTO detail : request.getDetails()) {
            PreparedPurchaseLine line = prepareLine(detail, productCache, inventoryCache);
            subtotal = subtotal.add(line.lineTotal);
            preparedLines.add(line);
        }

        // Guardar cabecera
        LocalDateTime now = LocalDateTime.now();
        Purchase purchase = new Purchase();
        purchase.setProviderId(provider.getProviderId());
        purchase.setPurchaseCode(purchaseCode);
        purchase.setPurchaseDate(purchaseDate);
        purchase.setStatus(STATUS_COMPLETED);
        purchase.setTotalAmount(subtotal);
        purchase.setNotes(request.getNotes());
        purchase.setCreatedAt(now);
        purchase.setUpdatedAt(now);

        Purchase savedPurchase = purchaseRepository.save(purchase);

        // Guardar detalles y actualizar inventario (SUMAR stock)
        List<PurchaseDetailResponseDTO> responseDetails = new ArrayList<>();

        for (PreparedPurchaseLine line : preparedLines) {
            // Sumar stock al inventario
            CurrentInventory inventory = line.inventory;
            BigDecimal newTotalStock = line.previousTotalStock.add(line.detail.getQuantityKg());
            BigDecimal newAvailableStock = line.previousAvailableStock.add(line.detail.getQuantityKg());
            inventory.setTotalStockKg(newTotalStock);
            inventory.setAvailableStockKg(newAvailableStock);
            currentInventoryRepository.save(inventory);

            // Guardar detalle de compra
            PurchaseDetail purchaseDetail = new PurchaseDetail();
            purchaseDetail.setPurchaseId(savedPurchase.getPurchaseId());
            purchaseDetail.setProductId(line.product.getProductId());
            purchaseDetail.setQuantityKg(line.detail.getQuantityKg());
            purchaseDetail.setUnitPrice(line.detail.getUnitPrice());
            purchaseDetailRepository.save(purchaseDetail);

            responseDetails.add(new PurchaseDetailResponseDTO(
                    line.product.getProductId(),
                    line.product.getName(),
                    line.detail.getQuantityKg(),
                    line.detail.getUnitPrice(),
                    line.lineTotal,
                    newTotalStock));
        }

        // Construir response
        PurchaseResponseDTO response = new PurchaseResponseDTO();
        response.setPurchaseId(savedPurchase.getPurchaseId());
        response.setProviderId(provider.getProviderId());
        response.setProviderName(provider.getCompanyName());
        response.setProviderTaxId(provider.getTaxId());
        response.setPurchaseCode(savedPurchase.getPurchaseCode());
        response.setPurchaseDate(savedPurchase.getPurchaseDate());
        response.setStatus(savedPurchase.getStatus());
        response.setSubtotal(subtotal);
        response.setTotalAmount(subtotal);
        response.setNotes(savedPurchase.getNotes());
        response.setCreatedAt(savedPurchase.getCreatedAt());
        response.setDetails(responseDetails);
        return response;
    }

    @Override
    public List<PurchaseResponseDTO> findAll() {
        List<Purchase> purchases = purchaseRepository.findAll(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "purchaseDate"));
        List<PurchaseResponseDTO> result = new ArrayList<>();
        for (Purchase purchase : purchases) {
            result.add(toResponse(purchase));
        }
        return result;
    }

    @Override
    public List<PurchaseResponseDTO> findByProviderId(Integer providerId) {
        providerRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El proveedor con ID " + providerId + " no existe"));

        List<Purchase> purchases = purchaseRepository.findByProviderIdOrderByPurchaseDateDesc(providerId);
        List<PurchaseResponseDTO> result = new ArrayList<>();
        for (Purchase purchase : purchases) {
            result.add(toResponse(purchase));
        }
        return result;
    }

    @Override
    public PurchaseResponseDTO findById(Integer id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Orden de compra con ID " + id + " no existe"));
        return toResponse(purchase);
    }
    // ─── helpers ────────────────────────────────────────────────────────────────

    private PurchaseResponseDTO toResponse(Purchase purchase) {
        Provider provider = providerRepository.findById(purchase.getProviderId()).orElse(null);

        List<PurchaseDetail> details = purchaseDetailRepository.findByPurchaseId(purchase.getPurchaseId());
        List<PurchaseDetailResponseDTO> detailDTOs = new ArrayList<>();

        for (PurchaseDetail detail : details) {
            Product product = productRepository.findById(detail.getProductId()).orElse(null);
            String productName = product != null ? product.getName() : "Producto #" + detail.getProductId();
            BigDecimal lineTotal = detail.getQuantityKg().multiply(detail.getUnitPrice());
            detailDTOs.add(new PurchaseDetailResponseDTO(
                    detail.getProductId(),
                    productName,
                    detail.getQuantityKg(),
                    detail.getUnitPrice(),
                    lineTotal,
                    null));
        }

        PurchaseResponseDTO dto = new PurchaseResponseDTO();
        dto.setPurchaseId(purchase.getPurchaseId());
        dto.setProviderId(purchase.getProviderId());
        dto.setProviderName(provider != null ? provider.getCompanyName() : null);
        dto.setProviderTaxId(provider != null ? provider.getTaxId() : null);
        dto.setPurchaseCode(purchase.getPurchaseCode());
        dto.setPurchaseDate(purchase.getPurchaseDate());
        dto.setStatus(purchase.getStatus());
        dto.setSubtotal(purchase.getTotalAmount());
        dto.setTotalAmount(purchase.getTotalAmount());
        dto.setNotes(purchase.getNotes());
        dto.setCreatedAt(purchase.getCreatedAt());
        dto.setDetails(detailDTOs);
        return dto;
    }

    private PreparedPurchaseLine prepareLine(
            PurchaseDetailRequestDTO detail,
            Map<Integer, Product> productCache,
            Map<Integer, CurrentInventory> inventoryCache) {

        if (detail == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los detalles de la compra son obligatorios");
        }

        Integer productId = detail.getProductId();
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "productId es requerido en el detalle");
        }

        BigDecimal quantityKg = detail.getQuantityKg();
        if (quantityKg == null || quantityKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "quantityKg debe ser mayor que 0");
        }

        BigDecimal unitPrice = detail.getUnitPrice();
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "unitPrice debe ser mayor o igual a 0");
        }

        // Validar producto activo
        Product product = productCache.computeIfAbsent(productId, id ->
                productRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Producto con ID " + id + " no existe")));

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El producto " + product.getName() + " no está activo");
        }

        // Obtener inventario (debe existir)
        CurrentInventory inventory = inventoryCache.computeIfAbsent(productId, id ->
                currentInventoryRepository.findByProductId(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "No existe registro de inventario para el producto " + id)));

        BigDecimal lineTotal = quantityKg.multiply(unitPrice);
        return new PreparedPurchaseLine(
                product,
                inventory,
                detail,
                inventory.getTotalStockKg(),
                inventory.getAvailableStockKg(),
                lineTotal);
    }

    private void validateRequest(PurchaseRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La solicitud no puede ser nula");
        }
        if (request.getProviderId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "providerId es requerido");
        }
        if (request.getPurchaseCode() == null || request.getPurchaseCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "purchaseCode es requerido");
        }
        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe enviar al menos un detalle de producto");
        }
    }

    // ─── inner class ────────────────────────────────────────────────────────────

    private static final class PreparedPurchaseLine {
        private final Product product;
        private final CurrentInventory inventory;
        private final PurchaseDetailRequestDTO detail;
        private final BigDecimal previousTotalStock;
        private final BigDecimal previousAvailableStock;
        private final BigDecimal lineTotal;

        private PreparedPurchaseLine(Product product, CurrentInventory inventory,
                PurchaseDetailRequestDTO detail, BigDecimal previousTotalStock,
                BigDecimal previousAvailableStock, BigDecimal lineTotal) {
            this.product = product;
            this.inventory = inventory;
            this.detail = detail;
            this.previousTotalStock = previousTotalStock;
            this.previousAvailableStock = previousAvailableStock;
            this.lineTotal = lineTotal;
        }
    }
}
