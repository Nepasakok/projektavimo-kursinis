package autodviratis.rules;

import autodviratis.model.BikeType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BikeTypeRuleTest {

    private final BikeTypeRule rule = new BikeTypeRule();

    @Test
    void mountainBikeIsFound() {
        Optional<BikeType> result = rule.extract("Kalnų dviratis");

        assertEquals(Optional.of(BikeType.MOUNTAIN), result);
    }

    @Test
    void roadBikeIsFound() {
        Optional<BikeType> result = rule.extract("Plento dviratis");

        assertEquals(Optional.of(BikeType.ROAD), result);
    }

    @Test
    void cityBikeIsFound() {
        Optional<BikeType> result = rule.extract("Miesto dviratis");

        assertEquals(Optional.of(BikeType.CITY), result);
    }

    @Test
    void twoTypesAreNotSpecified() {
        Optional<BikeType> result = rule.extract("Kalnų arba plento dviratis");

        assertEquals(Optional.empty(), result);
    }
}
