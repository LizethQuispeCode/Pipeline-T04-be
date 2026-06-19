package vallegrande.edu.pe.visons.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import vallegrande.edu.pe.visons.model.Product;
import vallegrande.edu.pe.visons.service.ProductService;

@RestController
@RequestMapping("/v1/api/product")
@Tag(name = "Product API", description = "API for Product management")
public class ProductRest {

    private final ProductService productService;

    @Autowired
    public ProductRest(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"", "/"})
    @Operation(summary = "Get All Products", description = "Get All Products")
    public List<Product> findAll() {
        return productService.findAll();
    }

    @GetMapping("/state/{state}")
    @Operation(summary = "Get Product By STATE (A/1/true for active)", description = "Get Product By STATE")
    public List<Product> findByState(@PathVariable String state) {
        return productService.findByState(state);
    }

    @GetMapping("/search")
    @Operation(summary = "Search Products By Name", description = "Search Products By Name")
    public List<Product> findByName(@RequestParam String name) {
        return productService.findByName(name);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Product By ID", description = "Get Product By ID")
    public Optional<Product> findById(@PathVariable Integer id) {
        return productService.findById(id);
    }

    @PostMapping("/save")
    @Operation(summary = "Crear (POST) - (fecha-hora)", description = "Save Product")
    public Product save(@Valid @RequestBody Product product) {
        return productService.save(product);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Editar (PUT) - (fecha-hora)", description = "Update Product")
    public Product update(@PathVariable Integer id, @RequestBody Product product) {
        return productService.update(id, product);
    }
    @PatchMapping("/{id}")
    @Operation(summary = "Eliminar (lógico)    (PATCH) - (fecha-hora)", description = "Logical Delete Product")
    public Product delete(@PathVariable Integer id) {
        return productService.delete(id);
    }

    @PatchMapping("/restore/{id}")
    @Operation(summary = "Restaurar (lógico) (PATCH) - (fecha-hora).", description = "Logical Restore Product")
    public Product restore(@PathVariable Integer id) {
        return productService.restore(id);
    }
}