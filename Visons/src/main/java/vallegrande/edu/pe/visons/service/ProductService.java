package vallegrande.edu.pe.visons.service;

import java.util.List;
import java.util.Optional;

import vallegrande.edu.pe.visons.model.Product;

public interface ProductService {

    List<Product> findAll();

    List<Product> findByState(String state);

    List<Product> findByName(String name);

    Optional<Product> findById(Integer id);

    Product save(Product product);

    Product update(Integer id, Product product);

    Product delete(Integer id);

    Product restore(Integer id);
}