package vallegrande.edu.pe.visons.service;

import java.util.List;
import java.util.Optional;

import vallegrande.edu.pe.visons.model.Provider;

public interface ProviderService {

    List<Provider> findAll();

    List<Provider> findAllIncludingInactive();

    List<Provider> findByState(String state);

    Optional<Provider> findById(Integer id);

    Provider save(Provider provider);

    Provider update(Integer id, Provider provider);

    Provider delete(Integer id);

    Provider restore(Integer id);
}