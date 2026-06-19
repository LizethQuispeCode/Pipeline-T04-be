package vallegrande.edu.pe.visons.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import vallegrande.edu.pe.visons.model.Provider;
import vallegrande.edu.pe.visons.repository.ProviderRepository;
import vallegrande.edu.pe.visons.service.ProviderService;

@Slf4j
@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;

    @Autowired
    public ProviderServiceImpl(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Override
    public List<Provider> findAll() {
        return providerRepository.findByIsActive(true);
    }

    @Override
    public List<Provider> findAllIncludingInactive() {
        return providerRepository.findAll();
    }

    @Override
    public List<Provider> findByState(String state) {
        boolean active = "A".equalsIgnoreCase(state) || "1".equals(state) || "true".equalsIgnoreCase(state);
        return providerRepository.findByIsActive(active);
    }

    @Override
    public Optional<Provider> findById(Integer id) {
        return providerRepository.findById(id);
    }

    @Override
    public Provider save(Provider provider) {
        ensureUniqueProvider(null, provider);
        LocalDateTime now = LocalDateTime.now();
        provider.setProviderId(null);
        provider.setIsActive(true);
        provider.setCreatedBy(provider.getCreatedBy() != null ? provider.getCreatedBy() : "SYSTEM");
        provider.setCreatedAt(now);
        provider.setUpdatedBy(null);
        provider.setUpdatedAt(null);
        provider.setDeletedAt(null);
        provider.setRestoredAt(null);
        return providerRepository.save(provider);
    }

    @Override
    public Provider update(Integer id, Provider provider) {
        Provider existing = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + id));
        ensureUniqueProvider(id, provider);
        existing.setCompanyName(provider.getCompanyName());
        existing.setTaxId(provider.getTaxId());
        existing.setProductType(provider.getProductType());
        existing.setContactEmail(provider.getContactEmail());
        existing.setContactPhone(provider.getContactPhone());
        existing.setAddress(provider.getAddress());
        existing.setUpdatedBy(provider.getUpdatedBy() != null ? provider.getUpdatedBy() : "SYSTEM");
        existing.setUpdatedAt(LocalDateTime.now());
        return providerRepository.save(existing);
    }

    private void ensureUniqueProvider(Integer currentId, Provider provider) {
        if (provider == null) {
            throw new RuntimeException("Provider inválido");
        }

        if (provider.getTaxId() != null && !provider.getTaxId().isBlank()) {
            providerRepository.findByTaxIdIgnoreCase(provider.getTaxId().trim())
                    .filter(existing -> currentId == null || !existing.getProviderId().equals(currentId))
                    .ifPresent(existing -> { throw new RuntimeException("El RUC/TAX ID del proveedor ya existe"); });
        }

        if (provider.getContactEmail() != null && !provider.getContactEmail().isBlank()) {
            providerRepository.findByContactEmailIgnoreCase(provider.getContactEmail().trim())
                    .filter(existing -> currentId == null || !existing.getProviderId().equals(currentId))
                    .ifPresent(existing -> { throw new RuntimeException("El correo de contacto del proveedor ya existe"); });
        }
    }

    @Override
    public Provider delete(Integer id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + id));
        provider.setDeletedAt(LocalDateTime.now());
        provider.setIsActive(false);
        return providerRepository.save(provider);
    }

    @Override
    public Provider restore(Integer id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + id));
        provider.setRestoredAt(LocalDateTime.now());
        provider.setIsActive(true);
        return providerRepository.save(provider);
    }
}