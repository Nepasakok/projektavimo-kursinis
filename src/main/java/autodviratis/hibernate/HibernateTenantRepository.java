package autodviratis.hibernate;

import autodviratis.model.Tenant;
import autodviratis.service.TenantRepository;

import java.util.List;
import java.util.Optional;

public class HibernateTenantRepository implements TenantRepository {

    private final GenericHibernate genericHibernate;

    public HibernateTenantRepository(GenericHibernate genericHibernate) {
        this.genericHibernate = genericHibernate;
    }

    @Override
    public Optional<Tenant> findById(String tenantId) {
        return genericHibernate.getById(Tenant.class, tenantId);
    }

    @Override
    public List<Tenant> findAll() {
        return genericHibernate.getAllRecords(Tenant.class);
    }
}
