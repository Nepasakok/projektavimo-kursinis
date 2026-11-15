package autodviratis.sources;

import autodviratis.model.RawListing;
import autodviratis.model.Tenant;
import autodviratis.service.ListingSource;
import autodviratis.service.SourceUnavailableException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitchableListingSourceTest {

    private final Tenant siauliai = new Tenant("siauliu-dviraciai", "Šiaulių dviračių salonas");
    private final Tenant vilnius = new Tenant("vilniaus-ratai", "Vilniaus dviračių klubas");

    private static class OneListingSource implements ListingSource {

        @Override
        public String getName() {
            return "Skelbiu.lt";
        }

        @Override
        public List<RawListing> fetch(Tenant tenant) {
            return List.of(new RawListing("Skelbiu.lt", "Trek", "", new BigDecimal("450"),
                    "Šiauliai", "hash-1", "https://x.lt"));
        }
    }

    private final SwitchableListingSource source = new SwitchableListingSource(new OneListingSource());

    @Test
    void sourceIsEnabledByDefault() {
        assertTrue(source.isEnabled(siauliai));
    }

    @Test
    void enabledSourceReturnsWrappedListings() throws SourceUnavailableException {
        List<RawListing> result = source.fetch(siauliai);

        assertEquals(1, result.size());
    }

    @Test
    void disabledSourceIsUnavailableForThatTenant() {
        source.setEnabled(siauliai, false);

        assertThrows(SourceUnavailableException.class, () -> source.fetch(siauliai));
    }

    @Test
    void disablingForOneTenantKeepsItEnabledForOther() throws SourceUnavailableException {
        source.setEnabled(siauliai, false);

        assertTrue(source.isEnabled(vilnius));
        assertEquals(1, source.fetch(vilnius).size());
    }

    @Test
    void reEnablingRestoresSourceForThatTenant() throws SourceUnavailableException {
        source.setEnabled(siauliai, false);
        source.setEnabled(siauliai, true);

        assertEquals(1, source.fetch(siauliai).size());
    }

    @Test
    void nameIsTakenFromWrappedSource() {
        assertEquals("Skelbiu.lt", source.getName());
    }
}
