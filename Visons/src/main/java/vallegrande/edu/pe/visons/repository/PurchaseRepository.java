package vallegrande.edu.pe.visons.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vallegrande.edu.pe.visons.model.Purchase;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {

    Optional<Purchase> findByPurchaseCodeIgnoreCase(String purchaseCode);

    List<Purchase> findByProviderIdOrderByPurchaseDateDesc(Integer providerId);
}
