package autodviratis.web;

import autodviratis.model.BikeType;
import autodviratis.model.BrakeType;
import autodviratis.model.FrameMaterial;
import autodviratis.model.Listing;
import autodviratis.model.SourceLink;
import autodviratis.model.Tenant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListingRowTest {

    private final Tenant tenant = new Tenant("siauliu-dviraciai", "Šiaulių dviračių salonas");

    private Listing listing(double wheelSize, String frameSize, int gears, String city) {
        return new Listing(tenant, "Trek", new BigDecimal("450.00"), city, "hash-1", BikeType.MOUNTAIN,
                wheelSize, frameSize, BrakeType.DISC, gears, FrameMaterial.ALUMINIUM,
                new SourceLink("Skelbiu.lt", "https://x.lt"));
    }

    @Test
    void knownValuesAreFormattedInLithuanian() {
        ListingRow row = ListingRow.from(listing(29.0, "M", 21, "Šiauliai"));

        assertEquals("450 €", row.price());
        assertEquals("kalnų", row.type());
        assertEquals("29\"", row.wheelSize());
        assertEquals("M", row.frameSize());
        assertEquals("diskiniai", row.brakeType());
        assertEquals("21", row.gears());
        assertEquals("aliuminis", row.frameMaterial());
    }

    @Test
    void decimalWheelSizeUsesComma() {
        ListingRow row = ListingRow.from(listing(27.5, "M", 21, "Šiauliai"));

        assertEquals("27,5\"", row.wheelSize());
    }

    @Test
    void unknownValuesShowNotSpecified() {
        ListingRow row = ListingRow.from(listing(Listing.WHEEL_SIZE_NOT_SPECIFIED,
                Listing.FRAME_SIZE_NOT_SPECIFIED, Listing.GEARS_NOT_SPECIFIED, ""));

        assertEquals("nenurodyta", row.wheelSize());
        assertEquals("nenurodyta", row.frameSize());
        assertEquals("nenurodyta", row.gears());
        assertEquals("nenurodyta", row.city());
    }
}
