package autodviratis.rules;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WheelSizeRuleTest {

    private final WheelSizeRule rule = new WheelSizeRule();

    @Test
    void upperLimit29IsAccepted() {
        Optional<Double> result = rule.extract("Trek Marlin 29\" M");

        assertEquals(Optional.of(29.0), result);
    }

    @Test
    void aboveUpperLimit30IsNotSpecified() {
        Optional<Double> result = rule.extract("Dviratis 30\"");

        assertEquals(Optional.empty(), result);
    }

    @Test
    void lowerLimit12IsAccepted() {
        Optional<Double> result = rule.extract("Vaikiškas dviratis 12\"");

        assertEquals(Optional.of(12.0), result);
    }

    @Test
    void belowLowerLimit11IsNotSpecified() {
        Optional<Double> result = rule.extract("Dviratukas 11\"");

        assertEquals(Optional.empty(), result);
    }

    @Test
    void decimalSizeWithCommaAndWordIsParsed() {
        Optional<Double> result = rule.extract("Ratai 27,5 colių");

        assertEquals(Optional.of(27.5), result);
    }

    @Test
    void typographicQuoteIsAccepted() {
        Optional<Double> result = rule.extract("Ratai 12“");

        assertEquals(Optional.of(12.0), result);
    }

    @Test
    void noWheelSizeInTextIsNotSpecified() {
        Optional<Double> result = rule.extract("Kalnų dviratis, geros būklės");

        assertEquals(Optional.empty(), result);
    }

    @Test
    void differentSizesInSameTextAreNotSpecified() {
        Optional<Double> result = rule.extract("29\" priekyje, 27,5\" gale");

        assertEquals(Optional.empty(), result);
    }
}
