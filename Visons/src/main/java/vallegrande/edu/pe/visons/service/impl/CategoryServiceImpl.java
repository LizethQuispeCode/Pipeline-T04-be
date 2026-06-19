package vallegrande.edu.pe.visons.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import vallegrande.edu.pe.visons.model.Category;
import vallegrande.edu.pe.visons.repository.CategoryRepository;
import vallegrande.edu.pe.visons.service.CategoryService;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> findAll() {
        log.info("Listando todas las categorías");
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> findAllActive() {
        log.info("Listando categorías activas");
        return categoryRepository.findAllActive();
    }

    @Override
    public List<Category> findByState(Boolean state) {
        log.info("Listando categorías por estado: {}", state);
        return categoryRepository.findByState(state);
    }

    @Override
    public Optional<Category> findById(Integer id) {
        log.info("Buscando categoría por ID: {}", id);
        return categoryRepository.findById(id);
    }

    @Override
    public Category save(Category category) {
        log.info("Registrando nueva categoría: {}", category.getName());
        category.setCategoryId(null);
        category.setIsActive(true);
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Integer id, Category categoryDetails) {
        Optional<Category> existingCategory = categoryRepository.findById(id);
        if (existingCategory.isPresent()) {
            Category category = existingCategory.get();
            if (categoryDetails.getName() != null) {
                category.setName(categoryDetails.getName());
            }
            if (categoryDetails.getDescription() != null) {
                category.setDescription(categoryDetails.getDescription());
            }
            return categoryRepository.save(category);
        }
        throw new RuntimeException("Categoría no encontrada");
    }

    @Override
    public Category delete(Integer id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            Category cat = category.get();
            cat.setIsActive(false);
            return categoryRepository.save(cat);
        }
        throw new RuntimeException("Categoría no encontrada");
    }

    @Override
    public Category restore(Integer id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            Category cat = category.get();
            cat.setIsActive(true);
            return categoryRepository.save(cat);
        }
        throw new RuntimeException("Categoría no encontrada");
    }
}