package autodviratis.service;

import autodviratis.model.Tenant;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {

    Optional<Tenant> findById(String tenantId);

    List<Tenant> findAll();
}
