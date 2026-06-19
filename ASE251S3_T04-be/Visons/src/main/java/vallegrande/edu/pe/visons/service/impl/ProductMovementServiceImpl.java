package vallegrande.edu.pe.visons.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vallegrande.edu.pe.visons.dto.ProductMovementDetailRequestDTO;
import vallegrande.edu.pe.visons.dto.ProductMovementDetailResponseDTO;
import vallegrande.edu.pe.visons.dto.ProductMovementRequestDTO;
import vallegrande.edu.pe.visons.dto.ProductMovementResponseDTO;
import vallegrande.edu.pe.visons.model.CurrentInventory;
import vallegrande.edu.pe.visons.model.Customer;
import vallegrande.edu.pe.visons.model.Order;
import vallegrande.edu.pe.visons.model.OrderDetail;
import vallegrande.edu.pe.visons.model.Product;
import vallegrande.edu.pe.visons.repository.CurrentInventoryRepository;
import vallegrande.edu.pe.visons.repository.CustomerRepository;
import vallegrande.edu.pe.visons.repository.OrderDetailRepository;
import vallegrande.edu.pe.visons.repository.OrderRepository;
import vallegrande.edu.pe.visons.repository.ProductRepository;
import vallegrande.edu.pe.visons.service.ProductMovementService;

@Service
public class ProductMovementServiceImpl implements ProductMovementService {

    private static final String STATUS_COMPLETED = "Completed";

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final CurrentInventoryRepository currentInventoryRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public ProductMovementServiceImpl(
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            ProductRepository productRepository,
            CurrentInventoryRepository currentInventoryRepository,
            CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.currentInventoryRepository = currentInventoryRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public ProductMovementResponseDTO registerMovement(ProductMovementRequestDTO request) {
        validateRequest(request);

        Customer customer = customerRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El cliente no existe"));

        String orderCode = request.getOrderCode().trim();
        if (orderRepository.findByOrderCodeIgnoreCase(orderCode).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código de movimiento ya existe");
        }

        LocalDate orderDate = request.getOrderDate() == null ? LocalDate.now() : request.getOrderDate();

        Map<Integer, Product> productCache = new HashMap<>();
        Map<Integer, CurrentInventory> inventoryCache = new HashMap<>();
        List<PreparedMovementLine> preparedLines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ProductMovementDetailRequestDTO detail : request.getDetails()) {
            PreparedMovementLine preparedLine = prepareLine(detail, productCache, inventoryCache);
            subtotal = subtotal.add(preparedLine.lineTotal);
            preparedLines.add(preparedLine);
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderCode(orderCode);
        order.setOrderDate(orderDate);
        order.setIncoterm(request.getIncoterm());
        order.setStatus(STATUS_COMPLETED);

        Order savedOrder = orderRepository.save(order);

        List<ProductMovementDetailResponseDTO> responseDetails = new ArrayList<>();
        for (PreparedMovementLine preparedLine : preparedLines) {
            CurrentInventory inventory = preparedLine.inventory;
            inventory.setTotalStockKg(preparedLine.previousTotalStock.subtract(preparedLine.request.getQuantityKg()));
            inventory.setAvailableStockKg(preparedLine.previousAvailableStock.subtract(preparedLine.request.getQuantityKg()));
            currentInventoryRepository.save(inventory);

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(savedOrder.getOrderId());
            orderDetail.setProductId(preparedLine.product.getProductId());
            orderDetail.setQuantityKg(preparedLine.request.getQuantityKg());
            orderDetail.setUnitPrice(preparedLine.request.getUnitPrice());
            orderDetailRepository.save(orderDetail);

            responseDetails.add(new ProductMovementDetailResponseDTO(
                    preparedLine.product.getProductId(),
                    preparedLine.product.getName(),
                    preparedLine.request.getQuantityKg(),
                    preparedLine.request.getUnitPrice(),
                    preparedLine.lineTotal,
                    preparedLine.previousAvailableStock.subtract(preparedLine.request.getQuantityKg())));
        }

        ProductMovementResponseDTO response = new ProductMovementResponseDTO();
        response.setOrderId(savedOrder.getOrderId());
        response.setClientId(customer.getClientId());
        response.setClientName(customer.getCompanyName());
        response.setOrderCode(savedOrder.getOrderCode());
        response.setOrderDate(savedOrder.getOrderDate());
        response.setIncoterm(savedOrder.getIncoterm());
        response.setStatus(savedOrder.getStatus());
        response.setSubtotal(subtotal);
        response.setTotal(subtotal);
        response.setDetails(responseDetails);
        return response;
    }

    private PreparedMovementLine prepareLine(
            ProductMovementDetailRequestDTO detail,
            Map<Integer, Product> productCache,
            Map<Integer, CurrentInventory> inventoryCache) {

        if (detail == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los detalles del movimiento son obligatorios");
        }

        Integer productId = detail.getProductId();
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId es requerido en el detalle");
        }

        BigDecimal quantityKg = detail.getQuantityKg();
        if (quantityKg == null || quantityKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantityKg debe ser mayor que 0");
        }

        BigDecimal unitPrice = detail.getUnitPrice();
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unitPrice debe ser mayor o igual a 0");
        }

        Product product = productCache.computeIfAbsent(productId, id -> productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Producto con ID " + id + " no existe")));

        CurrentInventory inventory = inventoryCache.computeIfAbsent(productId, id -> currentInventoryRepository.findByProductId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe inventario para el producto " + id)));

        BigDecimal availableStock = inventory.getAvailableStockKg();
        BigDecimal totalStock = inventory.getTotalStockKg();
        if (availableStock == null || totalStock == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El inventario del producto " + productId + " no está completo");
        }
        if (availableStock.compareTo(quantityKg) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente para el producto " + product.getName());
        }
        if (totalStock.compareTo(quantityKg) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock total insuficiente para el producto " + product.getName());
        }

        BigDecimal lineTotal = quantityKg.multiply(unitPrice);
        return new PreparedMovementLine(product, inventory, detail, totalStock, availableStock, lineTotal);
    }

    private void validateRequest(ProductMovementRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud no puede ser nula");
        }
        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe enviar al menos un detalle");
        }
        if (request.getClientId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId es requerido");
        }
        if (request.getOrderCode() == null || request.getOrderCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderCode es requerido");
        }
    }

    private static final class PreparedMovementLine {
        private final Product product;
        private final CurrentInventory inventory;
        private final ProductMovementDetailRequestDTO request;
        private final BigDecimal previousTotalStock;
        private final BigDecimal previousAvailableStock;
        private final BigDecimal lineTotal;

        private PreparedMovementLine(Product product, CurrentInventory inventory, ProductMovementDetailRequestDTO request,
                BigDecimal previousTotalStock, BigDecimal previousAvailableStock, BigDecimal lineTotal) {
            this.product = product;
            this.inventory = inventory;
            this.request = request;
            this.previousTotalStock = previousTotalStock;
            this.previousAvailableStock = previousAvailableStock;
            this.lineTotal = lineTotal;
        }
    }
}