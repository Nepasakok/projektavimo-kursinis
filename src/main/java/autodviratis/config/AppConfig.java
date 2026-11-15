package autodviratis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import autodviratis.hibernate.CustomHibernate;
import autodviratis.hibernate.GenericHibernate;
import autodviratis.hibernate.HibernateListingRepository;
import autodviratis.hibernate.HibernateTenantRepository;
import autodviratis.rules.BikeTypeRule;
import autodviratis.rules.BrakeTypeRule;
import autodviratis.rules.ConflictResolver;
import autodviratis.rules.FrameMaterialRule;
import autodviratis.rules.FrameSizeRule;
import autodviratis.rules.GearsRule;
import autodviratis.rules.WheelSizeRule;
import autodviratis.service.DuplicateMerger;
import autodviratis.service.ListingNormalizer;
import autodviratis.service.ListingRepository;
import autodviratis.service.ListingService;
import autodviratis.service.ListingSource;
import autodviratis.service.TenantRepository;
import autodviratis.service.TenantService;
import autodviratis.sources.JsonFileListingSource;
import autodviratis.sources.SwitchableListingSource;
import autodviratis.utils.JsonFileReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

@Configuration
public class AppConfig {

    private static final ZoneId ZONE = ZoneId.of("Europe/Vilnius");
    private static final String SKELBIU_NAME = "Skelbiu.lt";
    private static final String SKELBIU_FILE = "skelbiu";
    private static final String FACEBOOK_NAME = "Facebook";
    private static final String FACEBOOK_FILE = "facebook";

    @Bean
    public Clock clock() {
        return Clock.system(ZONE);
    }

    @Bean
    public JsonFileReader jsonFileReader(ObjectMapper objectMapper) {
        return new JsonFileReader(objectMapper);
    }

    @Bean
    public SwitchableListingSource skelbiuSource(JsonFileReader jsonFileReader) {
        return new SwitchableListingSource(new JsonFileListingSource(SKELBIU_NAME, SKELBIU_FILE, jsonFileReader));
    }

    @Bean
    public SwitchableListingSource facebookSource(JsonFileReader jsonFileReader) {
        return new SwitchableListingSource(new JsonFileListingSource(FACEBOOK_NAME, FACEBOOK_FILE, jsonFileReader));
    }

    @Bean
    public DuplicateMerger duplicateMerger() {
        return new DuplicateMerger();
    }

    @Bean
    public ListingNormalizer listingNormalizer() {
        return new ListingNormalizer(new BikeTypeRule(), new WheelSizeRule(), new FrameSizeRule(),
                new BrakeTypeRule(), new GearsRule(), new FrameMaterialRule(), new ConflictResolver());
    }

    @Bean
    public GenericHibernate genericHibernate(EntityManagerFactory entityManagerFactory) {
        return new GenericHibernate(entityManagerFactory);
    }

    @Bean
    public CustomHibernate customHibernate(EntityManagerFactory entityManagerFactory) {
        return new CustomHibernate(entityManagerFactory);
    }

    @Bean
    public ListingRepository listingRepository(CustomHibernate customHibernate) {
        return new HibernateListingRepository(customHibernate);
    }

    @Bean
    public TenantRepository tenantRepository(GenericHibernate genericHibernate) {
        return new HibernateTenantRepository(genericHibernate);
    }

    @Bean
    public TenantService tenantService(TenantRepository tenantRepository) {
        return new TenantService(tenantRepository);
    }

    @Bean
    public ListingService listingService(ListingNormalizer normalizer, DuplicateMerger merger,
                                         List<ListingSource> sources, ListingRepository repository, Clock clock) {
        return new ListingService(normalizer, merger, sources, repository, clock);
    }

    @Bean
    public DataSeeder dataSeeder(GenericHibernate genericHibernate, ListingService listingService) {
        return new DataSeeder(genericHibernate, listingService);
    }
}
