package autodviratis.config;

import autodviratis.hibernate.GenericHibernate;
import autodviratis.model.Tenant;
import autodviratis.service.ListingService;
import autodviratis.service.RefreshResult;
import autodviratis.service.SourceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

import java.util.Optional;

public class DataSeeder implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DataSeeder.class);

    private static final String SIAULIAI_ID = "siauliu-dviraciai";
    private static final String SIAULIAI_NAME = "Šiaulių dviračių salonas";
    private static final String VILNIUS_ID = "vilniaus-ratai";
    private static final String VILNIUS_NAME = "Vilniaus dviračių klubas";

    private final GenericHibernate genericHibernate;
    private final ListingService listingService;

    public DataSeeder(GenericHibernate genericHibernate, ListingService listingService) {
        this.genericHibernate = genericHibernate;
        this.listingService = listingService;
    }

    @Override
    public void run(String... args) {
        seedTenant(SIAULIAI_ID, SIAULIAI_NAME);
        seedTenant(VILNIUS_ID, VILNIUS_NAME);
    }

    private void seedTenant(String id, String name) {
        Optional<Tenant> existing = genericHibernate.getById(Tenant.class, id);
        Tenant tenant;
        if (existing.isPresent()) {
            tenant = existing.get();
        } else {
            tenant = new Tenant(id, name);
            genericHibernate.create(tenant);
        }
        RefreshResult result = listingService.refresh(tenant);
        LOG.info("{}: sujungtų skelbimų {}", name, result.listingCount());
        for (SourceStatus status : result.sourceStatuses()) {
            LOG.info("{}: {}", name, status.message());
        }
    }
}
