package autodviratis.service;

import autodviratis.model.Tenant;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TenantServiceTest {

    private final Tenant siauliai = new Tenant("siauliu-dviraciai", "Šiaulių dviračių salonas");
    private final Tenant vilnius = new Tenant("vilniaus-ratai", "Vilniaus dviračių klubas");

    private static class FakeTenantRepository implements TenantRepository {

        private final List<Tenant> tenants;

        FakeTenantRepository(List<Tenant> tenants) {
            this.tenants = tenants;
        }

        @Override
        public Optional<Tenant> findById(String tenantId) {
            for (Tenant tenant : tenants) {
                if (tenant.getId().equals(tenantId)) {
                    return Optional.of(tenant);
                }
            }
            return Optional.empty();
        }

        @Override
        public List<Tenant> findAll() {
            return tenants;
        }
    }

    private final TenantService service = new TenantService(new FakeTenantRepository(List.of(siauliai, vilnius)));

    @Test
    void findByIdReturnsExistingTenant() {
        assertEquals(Optional.of(vilnius), service.findById("vilniaus-ratai"));
    }

    @Test
    void findByIdReturnsEmptyForUnknownTenant() {
        assertEquals(Optional.empty(), service.findById("nezinomas"));
    }

    @Test
    void findAllReturnsAllTenants() {
        assertEquals(List.of(siauliai, vilnius), service.findAll());
    }
}
