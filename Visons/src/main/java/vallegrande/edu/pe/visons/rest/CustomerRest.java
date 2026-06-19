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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import vallegrande.edu.pe.visons.model.Customer;
import vallegrande.edu.pe.visons.service.CustomerService;

@RestController
@RequestMapping("/v1/api/customer")
@Tag(name = "Customer API", description = "API for Customer management")
public class CustomerRest {

    private final CustomerService customerService;

    @Autowired
    public CustomerRest(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping({"", "/"})
    @Operation(summary = "Get All Customers", description = "Get All Customers")
    public List<Customer> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/state/{state}")
    @Operation(summary = "Get Customer By STATE (A/1/true for active)", description = "Get Customer By STATE")
    public List<Customer> findByState(@PathVariable String state) {
        return customerService.findByState(state);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Customer By ID", description = "Get Customer By ID")
    public Optional<Customer> findById(@PathVariable Integer id) {
        return customerService.findById(id);
    }

    @PostMapping("/save")
    @Operation(summary = "Crear (POST) - (fecha-hora)", description = "Save Customer")
    public Customer save(@Valid @RequestBody Customer customer) {
        return customerService.save(customer);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Editar (PUT) - (fecha-hora)", description = "Update Customer")
    public Customer update(@PathVariable Integer id, @Valid @RequestBody Customer customer) {
        return customerService.update(id, customer);
    }
    @PatchMapping("/{id}")
    @Operation(summary = "Eliminar (lógico)    (PATCH) - (fecha-hora)", description = "Logical Delete Customer")
    public Customer delete(@PathVariable Integer id) {
        return customerService.delete(id);
    }

    @PatchMapping("/restore/{id}")
    @Operation(summary = "Restaurar (lógico) (PATCH) - (fecha-hora).", description = "Logical Restore Customer")
    public Customer restore(@PathVariable Integer id) {
        return customerService.restore(id);
    }
}