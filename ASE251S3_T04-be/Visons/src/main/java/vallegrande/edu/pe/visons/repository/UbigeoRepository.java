package vallegrande.edu.pe.visons.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vallegrande.edu.pe.visons.model.Ubigeo;

@Repository
public interface UbigeoRepository extends JpaRepository<Ubigeo, Integer> {

    boolean existsByUbigeoIdAndIsActiveTrue(Integer ubigeoId);
}
