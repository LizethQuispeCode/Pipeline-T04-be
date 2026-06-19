package vallegrande.edu.pe.visons.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vallegrande.edu.pe.visons.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    List<Customer> findByIsActive(Boolean isActive);

    Optional<Customer> findByTaxIdIgnoreCase(String taxId);

    Optional<Customer> findByEmailIgnoreCase(String email);
}