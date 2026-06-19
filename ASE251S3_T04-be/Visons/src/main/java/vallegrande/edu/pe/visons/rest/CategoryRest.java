package vallegrande.edu.pe.visons.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import vallegrande.edu.pe.visons.model.Category;
import vallegrande.edu.pe.visons.service.CategoryService;

@RestController
@RequestMapping("/v1/api/category")
@Tag(name = "Category API", description = "API for Category management")
public class CategoryRest {

    private final CategoryService categoryService;

    @Autowired
    public CategoryRest(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping({"", "/"})
    @Operation(summary = "Get All Categories", description = "Get All Categories")
    public List<Category> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("/active")
    @Operation(summary = "Get All Active Categories", description = "Get All Active Categories")
    public List<Category> findAllActive() {
        return categoryService.findAllActive();
    }

    @GetMapping("/state/{state}")
    @Operation(summary = "Get Category By STATE", description = "Get Category By STATE")
    public List<Category> findByState(@PathVariable Boolean state) {
        return categoryService.findByState(state);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Category By ID", description = "Get Category By ID")
    public Optional<Category> findById(@PathVariable Integer id) {
        return categoryService.findById(id);
    }

    @PostMapping("/save")
    @Operation(summary = "Crear (POST) - (fecha-hora)", description = "Save Category")
    public Category save(@RequestBody Category category) {
        return categoryService.save(category);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Editar (PUT) - (fecha-hora)", description = "Update Category")
    public Category update(@PathVariable Integer id, @RequestBody Category category) {
        return categoryService.update(id, category);
    }
    @PatchMapping("/{id}")
    @Operation(summary = "Eliminar (lógico)    (PATCH) - (fecha-hora)", description = "Logical Delete Category")
    public Category delete(@PathVariable Integer id) {
        return categoryService.delete(id);
    }

    @PatchMapping("/restore/{id}")
    @Operation(summary = "Restaurar (lógico) (PATCH) - (fecha-hora).", description = "Logical Restore Category")
    public Category restore(@PathVariable Integer id) {
        return categoryService.restore(id);
    }
}