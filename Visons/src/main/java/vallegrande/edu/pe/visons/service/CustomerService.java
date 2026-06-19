package vallegrande.edu.pe.visons.service;

import java.util.List;
import java.util.Optional;

import vallegrande.edu.pe.visons.model.Customer;

public interface CustomerService {

    List<Customer> findAll();

    List<Customer> findByState(String state);

    Optional<Customer> findById(Integer id);

    Customer save(Customer customer);

    Customer update(Integer id, Customer customer);

    Customer delete(Integer id);

    Customer restore(Integer id);
}