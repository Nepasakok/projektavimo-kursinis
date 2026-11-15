package autodviratis.service;

import autodviratis.model.BikeType;
import autodviratis.model.Listing;
import autodviratis.model.RawListing;
import autodviratis.model.SortOrder;
import autodviratis.model.Tenant;
import autodviratis.rules.BikeTypeRule;
import autodviratis.rules.BrakeTypeRule;
import autodviratis.rules.ConflictResolver;
import autodviratis.rules.FrameMaterialRule;
import autodviratis.rules.FrameSizeRule;
import autodviratis.rules.GearsRule;
import autodviratis.rules.WheelSizeRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListingServiceTest {

    private static final String SKELBIU = "Skelbiu.lt";
    private static final String FACEBOOK = "Facebook";
    private static final String TREK_TITLE = "Trek Marlin 29\" M kalnų dviratis";

    private final Tenant siauliai = new Tenant("siauliu-dviraciai", "Šiaulių dviračių salonas");
    private final Tenant vilnius = new Tenant("vilniaus-ratai", "Vilniaus dviračių klubas");

    private final Clock clock1430 = Clock.fixed(Instant.parse("2026-11-12T14:30:00Z"), ZoneOffset.UTC);
    private final Clock clock1500 = Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZoneOffset.UTC);

    private FakeListingSource skelbiu;
    private FakeListingSource facebook;
    private FakeListingRepository repository;
    private ListingService service;

    @BeforeEach
    void setUp() {
        skelbiu = new FakeListingSource(SKELBIU);
        facebook = new FakeListingSource(FACEBOOK);
        repository = new FakeListingRepository();
        service = createService(clock1430);
    }

    private ListingService createService(Clock clock) {
        ListingNormalizer normalizer = new ListingNormalizer(
                new BikeTypeRule(), new WheelSizeRule(), new FrameSizeRule(), new BrakeTypeRule(),
                new GearsRule(), new FrameMaterialRule(), new ConflictResolver());
        return new ListingService(normalizer, new DuplicateMerger(), List.of(skelbiu, facebook), repository, clock);
    }

    private RawListing raw(String source, String title, String price, String city, String photoHash) {
        return new RawListing(source, title, "", new BigDecimal(price), city, photoHash,
                "https://example.lt/" + source + "/" + photoHash);
    }

    private List<Listing> all(Tenant tenant) {
        return service.search(tenant, ListingFilter.noFilter(), SortOrder.NOT_SPECIFIED);
    }

    @Test
    void searchReturnsOnlyTrekForMountain29Under500InSiauliai() {
        skelbiu.setListings(siauliai, List.of(
                raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek"),
                raw(SKELBIU, "Kross Level 27,5\" S kalnų dviratis", "380", "Šiauliai", "hash-kross"),
                raw(SKELBIU, "Specialized Rockhopper 29\" L kalnų dviratis", "620", "Šiauliai", "hash-spec")));
        service.refresh(siauliai);
        ListingFilter filter = new ListingFilter(BikeType.MOUNTAIN, Optional.of(29.0), Optional.empty(),
                Optional.of(new BigDecimal("500")), Optional.of("Šiauliai"), false);

        List<Listing> result = service.search(siauliai, filter, SortOrder.NOT_SPECIFIED);

        assertEquals(1, result.size());
        assertEquals(TREK_TITLE, result.get(0).getTitle());
    }

    @Test
    void notBikeListingIsNotSaved() {
        skelbiu.setListings(siauliai, List.of(
                raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek"),
                raw(SKELBIU, "Sofa su pagalvėlėmis", "100", "Šiauliai", "hash-sofa")));

        RefreshResult result = service.refresh(siauliai);

        assertEquals(1, result.listingCount());
    }

    @Test
    void searchSortsByPriceAscending() {
        skelbiu.setListings(siauliai, List.of(
                raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek"),
                raw(SKELBIU, "Kross Level 27,5\" S kalnų dviratis", "380", "Šiauliai", "hash-kross")));
        service.refresh(siauliai);

        List<Listing> result = service.search(siauliai, ListingFilter.noFilter(), SortOrder.PRICE_ASC);

        assertEquals(new BigDecimal("380"), result.get(0).getPrice());
        assertEquals(new BigDecimal("450"), result.get(1).getPrice());
    }

    @Test
    void duplicateAcrossSourcesIsMergedWithLowerPriceAndBothLinks() {
        skelbiu.setListings(siauliai, List.of(raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek")));
        facebook.setListings(siauliai, List.of(raw(FACEBOOK, TREK_TITLE, "440", "Šiauliai", "hash-trek")));

        service.refresh(siauliai);

        List<Listing> result = all(siauliai);
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("440"), result.get(0).getPrice());
        assertEquals(2, result.get(0).getSourceLinks().size());
    }


    @Test
    void unavailableSourceKeepsOldListingsAndReportsExactMessage() {
        skelbiu.setListings(siauliai, List.of(
                raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek"),
                raw(SKELBIU, "Kross Level 27,5\" S kalnų dviratis", "380", "Šiauliai", "hash-kross")));
        facebook.setListings(siauliai, List.of(
                raw(FACEBOOK, "Giant Talon 29\" L kalnų dviratis", "500", "Šiauliai", "hash-giant")));
        service.refresh(siauliai);
        skelbiu.setAvailable(false);
        facebook.setListings(siauliai, List.of(
                raw(FACEBOOK, "Giant Talon 29\" L kalnų dviratis", "500", "Šiauliai", "hash-giant"),
                raw(FACEBOOK, "Cube Aim 29\" M kalnų dviratis", "550", "Šiauliai", "hash-cube")));

        RefreshResult result = createService(clock1500).refresh(siauliai);

        assertEquals(4, result.listingCount());
        assertEquals("Skelbiu.lt nepasiekiamas. Rodomi ankstesni duomenys, paskutinį kartą atnaujinti 2026-11-12 14:30.",
                result.sourceStatuses().get(0).message());
        assertEquals("Facebook: duomenys atnaujinti 2026-11-12 15:00 (2 skelbimai)",
                result.sourceStatuses().get(1).message());
    }

    @Test
    void unavailableSourceWithoutOldDataReportsNoPreviousData() {
        skelbiu.setAvailable(false);
        facebook.setListings(siauliai, List.of(raw(FACEBOOK, TREK_TITLE, "440", "Šiauliai", "hash-trek")));

        RefreshResult result = service.refresh(siauliai);

        assertEquals("Skelbiu.lt nepasiekiamas, ankstesnių duomenų nėra.", result.sourceStatuses().get(0).message());
        assertEquals(1, result.listingCount());
    }

    @Test
    void noDataFromAnySourceThrowsNoDataAvailable() {
        skelbiu.setAvailable(false);
        facebook.setAvailable(false);

        NoDataAvailableException exception = assertThrows(NoDataAvailableException.class,
                () -> service.refresh(siauliai));

        assertEquals("Duomenų gauti nepavyko", exception.getMessage());
    }

    @Test
    void tenantDoesNotSeeOtherTenantListings() {
        skelbiu.setListings(siauliai, List.of(raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek")));
        skelbiu.setListings(vilnius, List.of(raw(SKELBIU, "Kross Level 27,5\" S kalnų dviratis", "380", "Vilnius", "hash-kross")));
        service.refresh(siauliai);
        service.refresh(vilnius);

        List<Listing> result = all(siauliai);

        assertEquals(1, result.size());
        assertEquals(TREK_TITLE, result.get(0).getTitle());
    }

    @Test
    void hidingOtherTenantListingThrowsAndLeavesItUnchanged() {
        skelbiu.setListings(siauliai, List.of(raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek")));
        skelbiu.setListings(vilnius, List.of(raw(SKELBIU, "Kross Level 27,5\" S kalnų dviratis", "380", "Vilnius", "hash-kross")));
        service.refresh(siauliai);
        service.refresh(vilnius);
        Long vilniusListingId = all(vilnius).get(0).getId();

        assertThrows(ListingNotFoundException.class, () -> service.hide(siauliai, vilniusListingId));

        List<Listing> vilniusListings = all(vilnius);
        assertEquals(1, vilniusListings.size());
        assertEquals(new BigDecimal("380"), vilniusListings.get(0).getPrice());
    }

    @Test
    void hidingOwnListingRemovesIt() {
        skelbiu.setListings(siauliai, List.of(raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek")));
        service.refresh(siauliai);
        Long id = all(siauliai).get(0).getId();

        service.hide(siauliai, id);

        assertEquals(0, all(siauliai).size());
    }

    @Test
    void sourceStatusesAreEmptyBeforeFirstRefresh() {
        assertEquals(0, service.getSourceStatuses(siauliai).size());
    }

    @Test
    void sourceStatusesAreAvailableAfterRefresh() {
        skelbiu.setListings(siauliai, List.of(raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek")));
        facebook.setListings(siauliai, List.of(raw(FACEBOOK, TREK_TITLE, "440", "Šiauliai", "hash-trek")));

        service.refresh(siauliai);

        List<SourceStatus> statuses = service.getSourceStatuses(siauliai);
        assertEquals("Skelbiu.lt: duomenys atnaujinti 2026-11-12 14:30 (1 skelbimas)", statuses.get(0).message());
        assertEquals("Facebook: duomenys atnaujinti 2026-11-12 14:30 (1 skelbimas)", statuses.get(1).message());
    }

    @Test
    void sourceStatusesOfOneTenantAreNotVisibleToOther() {
        skelbiu.setListings(siauliai, List.of(raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek")));
        service.refresh(siauliai);

        assertEquals(0, service.getSourceStatuses(vilnius).size());
    }

    @Test
    void sourceStatusesShowUnavailableSourceAfterFailedRefresh() {
        skelbiu.setListings(siauliai, List.of(raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek")));
        service.refresh(siauliai);
        skelbiu.setAvailable(false);
        ListingService laterService = createService(clock1500);
        laterService.refresh(siauliai);

        List<SourceStatus> statuses = laterService.getSourceStatuses(siauliai);

        assertEquals("Skelbiu.lt nepasiekiamas. Rodomi ankstesni duomenys, paskutinį kartą atnaujinti 2026-11-12 14:30.",
                statuses.get(0).message());
    }

    @Test
    void repeatedRefreshDoesNotAccumulateDuplicates() {
        skelbiu.setListings(siauliai, List.of(raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek")));
        facebook.setListings(siauliai, List.of(raw(FACEBOOK, TREK_TITLE, "440", "Šiauliai", "hash-trek")));

        service.refresh(siauliai);
        service.refresh(siauliai);

        List<Listing> result = all(siauliai);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getSourceLinks().size());
    }

    @Test
    void repeatedRefreshDoesNotKeepPriceFromPreviousMerge() {
        skelbiu.setListings(siauliai, List.of(raw(SKELBIU, TREK_TITLE, "450", "Šiauliai", "hash-trek")));
        facebook.setListings(siauliai, List.of(raw(FACEBOOK, TREK_TITLE, "440", "Šiauliai", "hash-trek")));
        service.refresh(siauliai);
        facebook.setListings(siauliai, List.of(raw(FACEBOOK, TREK_TITLE, "460", "Šiauliai", "hash-trek")));

        service.refresh(siauliai);

        assertEquals(new BigDecimal("450"), all(siauliai).get(0).getPrice());
    }
}
