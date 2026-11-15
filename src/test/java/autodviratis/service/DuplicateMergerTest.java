package autodviratis.service;

import autodviratis.model.BikeType;
import autodviratis.model.BrakeType;
import autodviratis.model.FrameMaterial;
import autodviratis.model.Listing;
import autodviratis.model.SourceLink;
import autodviratis.model.Tenant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DuplicateMergerTest {

    private static final String SKELBIU = "Skelbiu.lt";
    private static final String FACEBOOK = "Facebook";

    private final Tenant tenant = new Tenant("siauliu-dviraciai", "Šiaulių dviračių salonas");
    private final DuplicateMerger merger = new DuplicateMerger();

    private Listing listing(Tenant owner, String source, String title, String price, String city, String photoHash) {
        return new Listing(owner, title, new BigDecimal(price), city, photoHash,
                BikeType.MOUNTAIN, 29.0, "M", BrakeType.DISC, 21, FrameMaterial.ALUMINIUM,
                new SourceLink(source, "https://example.lt/" + source));
    }

    private Listing listing(String source, String title, String price, String city, String photoHash) {
        return listing(tenant, source, title, price, city, photoHash);
    }

    @Test
    void samePhotoAndCityAreMergedWithLowerPrice() {
        Listing skelbiu = listing(SKELBIU, "Trek Marlin 29\" M", "450", "Šiauliai", "hash-1");
        Listing facebook = listing(FACEBOOK, "Trek Marlin 29\" M", "440", "Šiauliai", "hash-1");

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("440"), result.get(0).getPrice());
    }

    @Test
    void mergedListingKeepsBothSourceLinks() {
        Listing skelbiu = listing(SKELBIU, "Trek Marlin 29\" M", "450", "Šiauliai", "hash-1");
        Listing facebook = listing(FACEBOOK, "Trek Marlin 29\" M", "440", "Šiauliai", "hash-1");

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(2, result.get(0).getSourceLinks().size());
    }

    @Test
    void lowerPriceIsKeptWhenFirstListingIsCheaper() {
        Listing skelbiu = listing(SKELBIU, "Trek", "440", "Šiauliai", "hash-1");
        Listing facebook = listing(FACEBOOK, "Trek", "450", "Šiauliai", "hash-1");

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(new BigDecimal("440"), result.get(0).getPrice());
    }

    @Test
    void samePhotoDifferentCityAreNotMerged() {
        Listing skelbiu = listing(SKELBIU, "Trek", "450", "Šiauliai", "hash-1");
        Listing facebook = listing(FACEBOOK, "Trek", "440", "Vilnius", "hash-1");

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(2, result.size());
    }

    @Test
    void cityComparisonIgnoresCase() {
        Listing skelbiu = listing(SKELBIU, "Trek", "450", "Šiauliai", "hash-1");
        Listing facebook = listing(FACEBOOK, "Trek", "440", "ŠIAULIAI", "hash-1");

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(1, result.size());
    }

    @Test
    void differentPhotosAreNotMergedEvenWithSameTitleAndPrice() {
        Listing skelbiu = listing(SKELBIU, "Trek Marlin", "450", "Šiauliai", "hash-1");
        Listing facebook = listing(FACEBOOK, "Trek Marlin", "450", "Šiauliai", "hash-2");

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(2, result.size());
    }

    @Test
    void sameSourceIsNotMerged() {
        Listing first = listing(SKELBIU, "Trek", "450", "Šiauliai", "hash-1");
        Listing second = listing(SKELBIU, "Trek", "450", "Šiauliai", "hash-1");

        List<Listing> result = merger.merge(List.of(first, second));

        assertEquals(2, result.size());
    }

    @Test
    void differentTenantIsNotMerged() {
        Tenant other = new Tenant("vilniaus-ratai", "Vilniaus dviračių klubas");
        Listing first = listing(tenant, SKELBIU, "Trek", "450", "Šiauliai", "hash-1");
        Listing second = listing(other, FACEBOOK, "Trek", "450", "Šiauliai", "hash-1");

        List<Listing> result = merger.merge(List.of(first, second));

        assertEquals(2, result.size());
    }

    @Test
    void withoutPhotoSimilarTitleAndExactly10PercentPriceDifferenceAreMerged() {
        Listing skelbiu = listing(SKELBIU, "Trek Marlin 29 M", "100", "Šiauliai", "hash-1");
        Listing facebook = listing(FACEBOOK, "Trek Marlin 29 M", "110", "Šiauliai", null);

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("100"), result.get(0).getPrice());
    }

    @Test
    void withoutPhoto11PercentPriceDifferenceIsNotMerged() {
        Listing skelbiu = listing(SKELBIU, "Trek Marlin 29 M", "100", "Šiauliai", null);
        Listing facebook = listing(FACEBOOK, "Trek Marlin 29 M", "111", "Šiauliai", null);

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(2, result.size());
    }

    @Test
    void priceDifferenceIsMeasuredFromLowerPrice() {
        // 110 → 100: skirtumas 10 / 100 = 10 % (ne 10 / 110 = 9,1 %)
        Listing skelbiu = listing(SKELBIU, "Trek Marlin 29 M", "110", "Šiauliai", null);
        Listing facebook = listing(FACEBOOK, "Trek Marlin 29 M", "100", "Šiauliai", null);

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(1, result.size());
    }

    @Test
    void withoutPhotoTitleSimilarityExactly080IsMerged() {
        // 4 bendri žodžiai iš 5 skirtingų = 0,80
        Listing skelbiu = listing(SKELBIU, "Trek Marlin 29 M", "450", "Šiauliai", null);
        Listing facebook = listing(FACEBOOK, "Trek Marlin 29 M juodas", "450", "Šiauliai", null);

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(1, result.size());
    }

    @Test
    void withoutPhotoTitleSimilarityBelow080IsNotMerged() {
        // 4 bendri žodžiai iš 6 skirtingų = 0,67
        Listing skelbiu = listing(SKELBIU, "Trek Marlin 29 M", "450", "Šiauliai", null);
        Listing facebook = listing(FACEBOOK, "Trek Marlin 29 M juodas naujas", "450", "Šiauliai", null);

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(2, result.size());
    }

    @Test
    void ignoredWordsAndPunctuationDoNotAffectSimilarity() {
        Listing skelbiu = listing(SKELBIU, "Parduodu dviratis Trek, Marlin!", "450", "Šiauliai", null);
        Listing facebook = listing(FACEBOOK, "trek marlin", "450", "Šiauliai", null);

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(1, result.size());
    }

    @Test
    void withoutPhotoDifferentCityIsNotMerged() {
        Listing skelbiu = listing(SKELBIU, "Trek Marlin", "450", "Šiauliai", null);
        Listing facebook = listing(FACEBOOK, "Trek Marlin", "450", "Vilnius", null);

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(2, result.size());
    }

    @Test
    void unspecifiedFieldIsTakenFromOtherListing() {
        Listing skelbiu = listing(SKELBIU, "Trek", "450", "Šiauliai", "hash-1");
        skelbiu.setWheelSize(Listing.WHEEL_SIZE_NOT_SPECIFIED);
        Listing facebook = listing(FACEBOOK, "Trek", "440", "Šiauliai", "hash-1");

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(29.0, result.get(0).getWheelSize());
    }

    @Test
    void specifiedFieldOfFirstListingIsKept() {
        Listing skelbiu = listing(SKELBIU, "Trek", "450", "Šiauliai", "hash-1");
        skelbiu.setWheelSize(27.5);
        Listing facebook = listing(FACEBOOK, "Trek", "440", "Šiauliai", "hash-1");

        List<Listing> result = merger.merge(List.of(skelbiu, facebook));

        assertEquals(27.5, result.get(0).getWheelSize());
    }

    @Test
    void differentBikesAreAllKept() {
        Listing trek = listing(SKELBIU, "Trek Marlin", "450", "Šiauliai", "hash-1");
        Listing kross = listing(FACEBOOK, "Kross Level", "380", "Šiauliai", "hash-2");

        List<Listing> result = merger.merge(List.of(trek, kross));

        assertEquals(2, result.size());
    }
}
