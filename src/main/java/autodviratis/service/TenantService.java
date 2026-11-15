package autodviratis.service;

import autodviratis.model.Tenant;

import java.util.List;
import java.util.Optional;

public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Optional<Tenant> findById(String tenantId) {
        return tenantRepository.findById(tenantId);
    }

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }
}
