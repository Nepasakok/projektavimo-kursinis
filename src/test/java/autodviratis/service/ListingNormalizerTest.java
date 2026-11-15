package autodviratis.service;

import autodviratis.model.BikeType;
import autodviratis.model.BrakeType;
import autodviratis.model.FrameMaterial;
import autodviratis.model.Listing;
import autodviratis.model.RawListing;
import autodviratis.model.Tenant;
import autodviratis.rules.BikeTypeRule;
import autodviratis.rules.BrakeTypeRule;
import autodviratis.rules.ConflictResolver;
import autodviratis.rules.FrameMaterialRule;
import autodviratis.rules.FrameSizeRule;
import autodviratis.rules.GearsRule;
import autodviratis.rules.WheelSizeRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListingNormalizerTest {

    private final Tenant tenant = new Tenant("siauliu-dviraciai", "Šiaulių dviračių salonas");
    private final ListingNormalizer normalizer = new ListingNormalizer(
            new BikeTypeRule(), new WheelSizeRule(), new FrameSizeRule(), new BrakeTypeRule(),
            new GearsRule(), new FrameMaterialRule(), new ConflictResolver());

    private RawListing raw(String title, String description) {
        return new RawListing("Skelbiu.lt", title, description, new BigDecimal("450"),
                "Šiauliai", "hash-1", "https://skelbiu.lt/1");
    }

    @Test
    void allParametersAreExtracted() {
        RawListing raw = raw("Trek Marlin 29\" M kalnų dviratis",
                "Diskiniai stabdžiai, 21 pavarų, aliuminis rėmas");

        Listing listing = normalizer.normalize(tenant, raw).orElseThrow();

        assertEquals(BikeType.MOUNTAIN, listing.getType());
        assertEquals(29.0, listing.getWheelSize());
        assertEquals("M", listing.getFrameSize());
        assertEquals(BrakeType.DISC, listing.getBrakeType());
        assertEquals(21, listing.getGears());
        assertEquals(FrameMaterial.ALUMINIUM, listing.getFrameMaterial());
    }

    @Test
    void basicFieldsAndSourceLinkAreCopied() {
        RawListing raw = raw("Kalnų dviratis", "");

        Listing listing = normalizer.normalize(tenant, raw).orElseThrow();

        assertEquals(tenant, listing.getTenant());
        assertEquals("Šiauliai", listing.getCity());
        assertEquals(new BigDecimal("450"), listing.getPrice());
        assertEquals("hash-1", listing.getPhotoHash());
        assertEquals(1, listing.getSourceLinks().size());
        assertEquals("Skelbiu.lt", listing.getSourceLinks().get(0).getSourceName());
    }

    @Test
    void missingParametersAreNotSpecified() {
        RawListing raw = raw("Kalnų dviratis", "Geros būklės");

        Listing listing = normalizer.normalize(tenant, raw).orElseThrow();

        assertEquals(Listing.WHEEL_SIZE_NOT_SPECIFIED, listing.getWheelSize());
        assertEquals(Listing.FRAME_SIZE_NOT_SPECIFIED, listing.getFrameSize());
        assertEquals(BrakeType.NOT_SPECIFIED, listing.getBrakeType());
        assertEquals(Listing.GEARS_NOT_SPECIFIED, listing.getGears());
        assertEquals(FrameMaterial.NOT_SPECIFIED, listing.getFrameMaterial());
    }

    @Test
    void conflictBetweenTitleAndDescriptionIsNotSpecified() {
        RawListing raw = raw("Kalnų dviratis 29\"", "Ratai 27,5\"");

        Listing listing = normalizer.normalize(tenant, raw).orElseThrow();

        assertEquals(Listing.WHEEL_SIZE_NOT_SPECIFIED, listing.getWheelSize());
    }

    @Test
    void bikeWithoutKnownTypeHasTypeNotSpecified() {
        RawListing raw = raw("Dviratis Trek", "");

        Listing listing = normalizer.normalize(tenant, raw).orElseThrow();

        assertEquals(BikeType.NOT_SPECIFIED, listing.getType());
    }

    @Test
    void notBikeIsRejected() {
        RawListing raw = raw("Sofa su pagalvėlėmis", "Nauja, pigiai");

        Optional<Listing> result = normalizer.normalize(tenant, raw);

        assertTrue(result.isEmpty());
    }

    @Test
    void bikeWithBasketInTitleIsAccepted() {
        RawListing raw = raw("Miesto dviratis su krepšiu", "");

        Optional<Listing> result = normalizer.normalize(tenant, raw);

        assertTrue(result.isPresent());
    }

    @Test
    void bikeWithHelmetInTitleIsAccepted() {
        RawListing raw = raw("Kalnų dviratis + šalmas", "");

        Optional<Listing> result = normalizer.normalize(tenant, raw);

        assertTrue(result.isPresent());
    }

    @Test
    void helmetForCyclistIsRejected() {
        RawListing raw = raw("Šalmas dviratininkui", "");

        Optional<Listing> result = normalizer.normalize(tenant, raw);

        assertTrue(result.isEmpty());
    }

    @Test
    void bikePartIsRejected() {
        RawListing raw = raw("Dviračio padangos 29\"", "Kalnų dviračio padangos");

        Optional<Listing> result = normalizer.normalize(tenant, raw);

        assertTrue(result.isEmpty());
    }
}
