package autodviratis.service;

import autodviratis.model.BikeType;
import autodviratis.model.BrakeType;
import autodviratis.model.FrameMaterial;
import autodviratis.model.Listing;
import autodviratis.model.SourceLink;
import autodviratis.model.Tenant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListingFilterTest {

    private final Tenant tenant = new Tenant("siauliu-dviraciai", "Šiaulių dviračių salonas");

    private Listing listing(BikeType type, double wheelSize, String price, String city) {
        return new Listing(tenant, "Trek", new BigDecimal(price), city, "hash-1", type, wheelSize,
                "M", BrakeType.DISC, 21, FrameMaterial.ALUMINIUM, new SourceLink("Skelbiu.lt", "https://x.lt"));
    }

    private ListingFilter priceFilter(String min, String max) {
        return new ListingFilter(BikeType.NOT_SPECIFIED, Optional.empty(),
                Optional.ofNullable(min).map(BigDecimal::new), Optional.ofNullable(max).map(BigDecimal::new),
                Optional.empty(), false);
    }

    private ListingFilter wheelFilter(double wheelSize, boolean includeUnspecified) {
        return new ListingFilter(BikeType.NOT_SPECIFIED, Optional.of(wheelSize), Optional.empty(),
                Optional.empty(), Optional.empty(), includeUnspecified);
    }

    private ListingFilter typeFilter(BikeType type, boolean includeUnspecified) {
        return new ListingFilter(type, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), includeUnspecified);
    }

    private ListingFilter cityFilter(String city, boolean includeUnspecified) {
        return new ListingFilter(BikeType.NOT_SPECIFIED, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(city), includeUnspecified);
    }

    @Test
    void emptyFilterMatchesEverything() {
        Listing listing = listing(BikeType.NOT_SPECIFIED, Listing.WHEEL_SIZE_NOT_SPECIFIED, "450", "");

        assertTrue(ListingFilter.noFilter().matches(listing));
    }

    @Test
    void priceEqualToMaxIsIncluded() {
        Listing listing = listing(BikeType.MOUNTAIN, 29.0, "500", "Šiauliai");

        assertTrue(priceFilter(null, "500").matches(listing));
    }

    @Test
    void priceAboveMaxIsExcluded() {
        Listing listing = listing(BikeType.MOUNTAIN, 29.0, "500.01", "Šiauliai");

        assertFalse(priceFilter(null, "500").matches(listing));
    }

    @Test
    void priceEqualToMinIsIncluded() {
        Listing listing = listing(BikeType.MOUNTAIN, 29.0, "300", "Šiauliai");

        assertTrue(priceFilter("300", null).matches(listing));
    }

    @Test
    void priceBelowMinIsExcluded() {
        Listing listing = listing(BikeType.MOUNTAIN, 29.0, "299.99", "Šiauliai");

        assertFalse(priceFilter("300", null).matches(listing));
    }

    @Test
    void wheelSizeEqualIsIncluded() {
        Listing listing = listing(BikeType.MOUNTAIN, 29.0, "450", "Šiauliai");

        assertTrue(wheelFilter(29.0, false).matches(listing));
    }

    @Test
    void otherWheelSizeIsExcluded() {
        Listing listing = listing(BikeType.MOUNTAIN, 27.5, "450", "Šiauliai");

        assertFalse(wheelFilter(29.0, false).matches(listing));
    }

    @Test
    void unspecifiedWheelSizeIsExcludedWhenFilteringByWheelSize() {
        Listing listing = listing(BikeType.MOUNTAIN, Listing.WHEEL_SIZE_NOT_SPECIFIED, "450", "Šiauliai");

        assertFalse(wheelFilter(29.0, false).matches(listing));
    }

    @Test
    void unspecifiedWheelSizeIsIncludedWhenIncludeUnspecifiedIsOn() {
        Listing listing = listing(BikeType.MOUNTAIN, Listing.WHEEL_SIZE_NOT_SPECIFIED, "450", "Šiauliai");

        assertTrue(wheelFilter(29.0, true).matches(listing));
    }

    @Test
    void unspecifiedTypeIsExcludedWhenFilteringByType() {
        Listing listing = listing(BikeType.NOT_SPECIFIED, 29.0, "450", "Šiauliai");

        assertFalse(typeFilter(BikeType.MOUNTAIN, false).matches(listing));
    }

    @Test
    void otherTypeIsExcluded() {
        Listing listing = listing(BikeType.ROAD, 29.0, "450", "Šiauliai");

        assertFalse(typeFilter(BikeType.MOUNTAIN, false).matches(listing));
    }

    @Test
    void sameTypeIsIncluded() {
        Listing listing = listing(BikeType.MOUNTAIN, 29.0, "450", "Šiauliai");

        assertTrue(typeFilter(BikeType.MOUNTAIN, false).matches(listing));
    }

    @Test
    void cityComparisonIgnoresCase() {
        Listing listing = listing(BikeType.MOUNTAIN, 29.0, "450", "Šiauliai");

        assertTrue(cityFilter("ŠIAULIAI", false).matches(listing));
    }

    @Test
    void otherCityIsExcluded() {
        Listing listing = listing(BikeType.MOUNTAIN, 29.0, "450", "Vilnius");

        assertFalse(cityFilter("Šiauliai", false).matches(listing));
    }

    @Test
    void unspecifiedCityIsExcludedWhenFilteringByCity() {
        Listing listing = listing(BikeType.MOUNTAIN, 29.0, "450", "");

        assertFalse(cityFilter("Šiauliai", false).matches(listing));
    }

    @Test
    void allSelectedFiltersMustMatch() {
        // Trek 29" 450 €, Šiauliai – atitinka viską, išskyrus kainą
        ListingFilter filter = new ListingFilter(BikeType.MOUNTAIN, Optional.of(29.0), Optional.empty(),
                Optional.of(new BigDecimal("400")), Optional.of("Šiauliai"), false);
        Listing listing = listing(BikeType.MOUNTAIN, 29.0, "450", "Šiauliai");

        assertFalse(filter.matches(listing));
    }

    @Test
    void planScenarioKeepsOnlyTrek() {
        ListingFilter filter = new ListingFilter(BikeType.MOUNTAIN, Optional.of(29.0), Optional.empty(),
                Optional.of(new BigDecimal("500")), Optional.of("Šiauliai"), false);
        Listing trek = listing(BikeType.MOUNTAIN, 29.0, "450", "Šiauliai");
        Listing kross = listing(BikeType.MOUNTAIN, 27.5, "380", "Šiauliai");
        Listing specialized = listing(BikeType.MOUNTAIN, 29.0, "620", "Šiauliai");

        assertTrue(filter.matches(trek));
        assertFalse(filter.matches(kross));
        assertFalse(filter.matches(specialized));
    }
}
