package vallegrande.edu.pe.visons.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import vallegrande.edu.pe.visons.model.CurrentInventory;
import vallegrande.edu.pe.visons.model.Product;
import vallegrande.edu.pe.visons.repository.CurrentInventoryRepository;
import vallegrande.edu.pe.visons.repository.ProductRepository;
import vallegrande.edu.pe.visons.service.ProductService;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CurrentInventoryRepository currentInventoryRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CurrentInventoryRepository currentInventoryRepository) {
        this.productRepository = productRepository;
        this.currentInventoryRepository = currentInventoryRepository;
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll().stream().map(this::withInventory).toList();
    }

    @Override
    public List<Product> findByState(String state) {
        boolean active = "A".equalsIgnoreCase(state) || "1".equals(state) || "true".equalsIgnoreCase(state);
        return productRepository.findByIsActive(active).stream().map(this::withInventory).toList();
    }

    @Override
    public List<Product> findByName(String name) {
        if (name == null || name.isBlank()) {
            return findAll();
        }

        String normalizedName = name.trim();
        return productRepository.findByNameContainingIgnoreCase(normalizedName).stream().map(this::withInventory).toList();
    }

    @Override
    public Optional<Product> findById(Integer id) {
        return productRepository.findById(id).map(this::withInventory);
    }

    @Override
    @Transactional
    public Product save(Product product) {
        ensureUniqueProduct(null, product);
        LocalDateTime now = LocalDateTime.now();
        product.setProductId(null);
        product.setIsActive(true);
        product.setCreatedAt(now);
        product.setUpdatedAt(null);
        product.setDeletedAt(null);
        product.setRestoredAt(null);
        if (product.getUnitMeasure() == null || product.getUnitMeasure().isBlank()) {
            product.setUnitMeasure("KG");
        }
        if (product.getIsOwnProduction() == null) {
            product.setIsOwnProduction(false);
        }
        Product savedProduct = productRepository.save(product);
        syncInventory(savedProduct, product.getInitialStockKg());
        return withInventory(savedProduct);
    }

    @Override
    @Transactional
    public Product update(Integer id, Product productDetails) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            BigDecimal desiredStockKg = productDetails.getInitialStockKg() != null
                ? productDetails.getInitialStockKg()
                : product.getInitialStockKg();
            if (desiredStockKg != null) {
                product.setInitialStockKg(desiredStockKg);
            }
            Integer targetCategoryId = productDetails.getCategoryId() != null
                    ? productDetails.getCategoryId()
                    : product.getCategoryId();
            String targetName = productDetails.getName() != null
                    ? productDetails.getName()
                    : product.getName();

            Product uniquenessProbe = new Product();
            uniquenessProbe.setCategoryId(targetCategoryId);
            uniquenessProbe.setName(targetName);
            ensureUniqueProduct(id, uniquenessProbe);
            if (productDetails.getCategoryId() != null) {
                product.setCategoryId(productDetails.getCategoryId());
            }
            if (productDetails.getName() != null) {
                product.setName(productDetails.getName());
            }
            if (productDetails.getVariety() != null) {
                product.setVariety(productDetails.getVariety());
            }
            if (productDetails.getCaliber() != null) {
                product.setCaliber(productDetails.getCaliber());
            }
            if (productDetails.getUnitMeasure() != null) {
                product.setUnitMeasure(productDetails.getUnitMeasure());
            }
            if (productDetails.getBoxWeightKg() != null) {
                product.setBoxWeightKg(productDetails.getBoxWeightKg());
            }
            if (productDetails.getIsOwnProduction() != null) {
                product.setIsOwnProduction(productDetails.getIsOwnProduction());
            }
            if (productDetails.getIsActive() != null) {
                product.setIsActive(productDetails.getIsActive());
                if (Boolean.TRUE.equals(productDetails.getIsActive())) {
                    product.setRestoredAt(LocalDateTime.now());
                    product.setDeletedAt(null);
                } else {
                    product.setDeletedAt(LocalDateTime.now());
                }
            }
            product.setUpdatedAt(LocalDateTime.now());
            Product savedProduct = productRepository.save(product);
            if (desiredStockKg != null) {
                syncInventory(savedProduct, desiredStockKg);
            }
            return withInventory(savedProduct);
        }
        throw new RuntimeException("Producto no encontrado");
    }

    private void syncInventory(Product product, BigDecimal desiredStockKg) {
        if (product == null || product.getProductId() == null) {
            return;
        }

        if (desiredStockKg == null) {
            throw new RuntimeException("El stock inicial es requerido");
        }

        BigDecimal normalizedStock = desiredStockKg;
        if (normalizedStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El stock inicial no puede ser negativo");
        }

        CurrentInventory inventory = currentInventoryRepository.findByProductId(product.getProductId())
                .orElseGet(() -> {
                    CurrentInventory createdInventory = new CurrentInventory();
                    createdInventory.setProductId(product.getProductId());
                    createdInventory.setReservedStockKg(BigDecimal.ZERO);
                    return createdInventory;
                });

        BigDecimal reservedStock = inventory.getReservedStockKg() == null ? BigDecimal.ZERO : inventory.getReservedStockKg();
        if (normalizedStock.compareTo(reservedStock) < 0) {
            throw new RuntimeException("El stock no puede ser menor que el stock reservado");
        }

        inventory.setTotalStockKg(normalizedStock);
        inventory.setReservedStockKg(reservedStock);
        inventory.setAvailableStockKg(normalizedStock.subtract(reservedStock));
        currentInventoryRepository.saveAndFlush(inventory);
    }

    private Product withInventory(Product product) {
        if (product == null || product.getProductId() == null) {
            return product;
        }

        currentInventoryRepository.findReadOnlyByProductId(product.getProductId()).ifPresentOrElse(inventory -> {
            product.setInitialStockKg(inventory.getTotalStockKg());
            product.setTotalStockKg(inventory.getTotalStockKg());
            product.setReservedStockKg(inventory.getReservedStockKg());
            product.setAvailableStockKg(inventory.getAvailableStockKg());
        }, () -> {
            product.setInitialStockKg(BigDecimal.ZERO);
            product.setTotalStockKg(BigDecimal.ZERO);
            product.setReservedStockKg(BigDecimal.ZERO);
            product.setAvailableStockKg(BigDecimal.ZERO);
        });
        return product;
    }

    private void ensureUniqueProduct(Integer currentId, Product product) {
        if (product == null || product.getCategoryId() == null || product.getName() == null || product.getName().isBlank()) {
            return;
        }

        productRepository.findByCategoryIdAndNameIgnoreCase(product.getCategoryId(), product.getName().trim())
                .filter(existing -> currentId == null || !existing.getProductId().equals(currentId))
                .ifPresent(existing -> { throw new RuntimeException("Ya existe un producto con ese nombre en la categoría seleccionada"); });
    }

    @Override
    @Transactional
    public Product delete(Integer id) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setIsActive(false);
            product.setDeletedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            return withInventory(productRepository.save(product));
        }
        throw new RuntimeException("Producto no encontrado");
    }

    @Override
    @Transactional
    public Product restore(Integer id) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setIsActive(true);
            product.setRestoredAt(LocalDateTime.now());
            product.setDeletedAt(null);
            product.setUpdatedAt(LocalDateTime.now());
            return withInventory(productRepository.save(product));
        }
        throw new RuntimeException("Producto no encontrado");
    }
}
