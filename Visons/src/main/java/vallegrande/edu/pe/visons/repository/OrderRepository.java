package vallegrande.edu.pe.visons.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vallegrande.edu.pe.visons.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

	Optional<Order> findByOrderCodeIgnoreCase(String orderCode);

	List<Order> findByCustomer_ClientIdOrderByOrderDateDesc(Integer clientId);

	List<Order> findByStatusIgnoreCaseOrderByOrderDateDesc(String status);
}