package autodviratis.rules;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameSizeRuleTest {

    private final FrameSizeRule rule = new FrameSizeRule();

    @Test
    void letterSizeIsFound() {
        Optional<String> result = rule.extract("Trek 29\" M");

        assertEquals(Optional.of("M"), result);
    }

    @Test
    void extraLargeIsNotMixedWithL() {
        Optional<String> result = rule.extract("Rėmo dydis XL");

        assertEquals(Optional.of("XL"), result);
    }

    @Test
    void centimetresNextToFrameWordAreFound() {
        Optional<String> result = rule.extract("Rėmas 54 cm");

        assertEquals(Optional.of("54 cm"), result);
    }

    @Test
    void centimetresWithoutFrameWordAreIgnored() {
        Optional<String> result = rule.extract("Ilgis 54 cm");

        assertEquals(Optional.empty(), result);
    }

    @Test
    void lowercaseLetterInsideWordIsIgnored() {
        Optional<String> result = rule.extract("Geros būklės dviratis");

        assertEquals(Optional.empty(), result);
    }

    @Test
    void differentSizesAreNotSpecified() {
        Optional<String> result = rule.extract("Dydis M, rėmas 54 cm");

        assertEquals(Optional.empty(), result);
    }
}
