package vallegrande.edu.pe.visons.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import vallegrande.edu.pe.visons.model.Customer;
import vallegrande.edu.pe.visons.repository.CustomerRepository;
import vallegrande.edu.pe.visons.service.CustomerService;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findByIsActive(true);
    }

    @Override
    public List<Customer> findByState(String state) {
        boolean active = "A".equalsIgnoreCase(state) || "1".equals(state) || "true".equalsIgnoreCase(state);
        return customerRepository.findByIsActive(active);
    }

    @Override
    public Optional<Customer> findById(Integer id) {
        return customerRepository.findById(id);
    }

    @Override
    public Customer save(Customer customer) {
        ensureUniqueCustomer(null, customer);
        LocalDateTime now = LocalDateTime.now();
        customer.setIsActive(true);
        customer.setCreatedAt(now);
        customer.setUpdatedAt(null);
        customer.setDeletedAt(null);
        customer.setRestoredAt(null);
        return customerRepository.save(customer);
    }

    @Override
    public Customer update(Integer id, Customer customerDetails) {
        Optional<Customer> existingCustomer = customerRepository.findById(id);
        if (existingCustomer.isPresent()) {
            Customer customer = existingCustomer.get();
            ensureUniqueCustomer(id, customerDetails);
            if (customerDetails.getCompanyName() != null) {
                customer.setCompanyName(customerDetails.getCompanyName());
            }
            if (customerDetails.getTaxId() != null) {
                customer.setTaxId(customerDetails.getTaxId());
            }
            if (customerDetails.getCountry() != null) {
                customer.setCountry(customerDetails.getCountry());
            }
            if (customerDetails.getPhone() != null) {
                customer.setPhone(customerDetails.getPhone());
            }
            if (customerDetails.getAddress() != null) {
                customer.setAddress(customerDetails.getAddress());
            }
            if (customerDetails.getEmail() != null) {
                customer.setEmail(customerDetails.getEmail());
            }
            if (customerDetails.getCreditLimit() != null) {
                customer.setCreditLimit(customerDetails.getCreditLimit());
            }
            System.out.println("Customer update phone request: " + customerDetails.getPhone());
            System.out.println("Customer update phone entity: " + customer.getPhone());
            customer.setUpdatedAt(LocalDateTime.now());
            return customerRepository.save(customer);
        }
        throw new RuntimeException("Cliente no encontrado");
    }

    private void ensureUniqueCustomer(Integer currentId, Customer customer) {
        if (customer == null) {
            throw new RuntimeException("Cliente inválido");
        }

        if (customer.getTaxId() != null && !customer.getTaxId().isBlank()) {
            customerRepository.findByTaxIdIgnoreCase(customer.getTaxId().trim())
                    .filter(existing -> currentId == null || !existing.getClientId().equals(currentId))
                    .ifPresent(existing -> { throw new RuntimeException("El RUC/TAX ID ya está registrado"); });
        }

        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            customerRepository.findByEmailIgnoreCase(customer.getEmail().trim())
                    .filter(existing -> currentId == null || !existing.getClientId().equals(currentId))
                    .ifPresent(existing -> { throw new RuntimeException("El correo ya está registrado"); });
        }
    }

    @Override
    public Customer delete(Integer id) {
        Optional<Customer> existingCustomer = customerRepository.findById(id);
        if (existingCustomer.isPresent()) {
            Customer customer = existingCustomer.get();
            customer.setDeletedAt(LocalDateTime.now());
            customer.setIsActive(false);
            return customerRepository.save(customer);
        }
        throw new RuntimeException("Cliente no encontrado");
    }

    @Override
    public Customer restore(Integer id) {
        Optional<Customer> existingCustomer = customerRepository.findById(id);
        if (existingCustomer.isPresent()) {
            Customer customer = existingCustomer.get();
            customer.setRestoredAt(LocalDateTime.now());
            customer.setIsActive(true);
            return customerRepository.save(customer);
        }
        throw new RuntimeException("Cliente no encontrado");
    }
}