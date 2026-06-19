package vallegrande.edu.pe.visons.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vallegrande.edu.pe.visons.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByIsActive(Boolean isActive);

    List<Product> findByNameContainingIgnoreCase(String name);

    Optional<Product> findByCategoryIdAndNameIgnoreCase(Integer categoryId, String name);
}