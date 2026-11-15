package autodviratis.web;

import autodviratis.model.BikeType;
import autodviratis.model.SortOrder;
import autodviratis.service.ListingFilter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilterFormTest {

    private final FilterForm form = new FilterForm();

    @Test
    void emptyFormGivesNoConditions() {
        ListingFilter filter = form.toFilter();

        assertEquals(BikeType.NOT_SPECIFIED, filter.getType());
        assertEquals(Optional.empty(), filter.getWheelSize());
        assertEquals(Optional.empty(), filter.getMaxPrice());
        assertEquals(Optional.empty(), filter.getCity());
    }

    @Test
    void typeIsParsedFromEnumName() {
        form.setType("MOUNTAIN");

        assertEquals(BikeType.MOUNTAIN, form.toFilter().getType());
    }

    @Test
    void unknownTypeIsIgnored() {
        form.setType("SPACESHIP");

        assertEquals(BikeType.NOT_SPECIFIED, form.toFilter().getType());
    }

    @Test
    void wheelSizeWithCommaIsParsed() {
        form.setWheelSize("27,5");

        assertEquals(Optional.of(27.5), form.toFilter().getWheelSize());
    }

    @Test
    void maxPriceIsParsed() {
        form.setMaxPrice("500");

        assertEquals(Optional.of(new BigDecimal("500")), form.toFilter().getMaxPrice());
    }

    @Test
    void cityIsTrimmed() {
        form.setCity("  Šiauliai ");

        assertEquals(Optional.of("Šiauliai"), form.toFilter().getCity());
    }

    @Test
    void invalidNumberIsIgnoredAndReported() {
        form.setMinPrice("abc");

        assertEquals(Optional.empty(), form.toFilter().getMinPrice());
        assertEquals(List.of("Kaina nuo"), form.invalidFields());
    }

    @Test
    void sortOrderIsParsed() {
        form.setSort("PRICE_DESC");

        assertEquals(SortOrder.PRICE_DESC, form.toSortOrder());
    }

    @Test
    void unknownSortOrderIsNotSpecified() {
        form.setSort("RANDOM");

        assertEquals(SortOrder.NOT_SPECIFIED, form.toSortOrder());
    }
}
