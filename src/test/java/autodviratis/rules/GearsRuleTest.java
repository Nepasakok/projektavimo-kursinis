package autodviratis.rules;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GearsRuleTest {

    private final GearsRule rule = new GearsRule();

    @Test
    void numberBeforeGearsWordIsFound() {
        Optional<Integer> result = rule.extract("21 pavarų");

        assertEquals(Optional.of(21), result);
    }

    @Test
    void numberBeforeAlternativeWordIsFound() {
        Optional<Integer> result = rule.extract("7 bėgių");

        assertEquals(Optional.of(7), result);
    }

    @Test
    void numberAfterGearsWordIsFound() {
        Optional<Integer> result = rule.extract("Pavarų: 24");

        assertEquals(Optional.of(24), result);
    }

    @Test
    void numberAboveLimitIsNotSpecified() {
        Optional<Integer> result = rule.extract("99 pavarų");

        assertEquals(Optional.empty(), result);
    }

    @Test
    void noGearsMentionedIsNotSpecified() {
        Optional<Integer> result = rule.extract("Kaina 450 eurų");

        assertEquals(Optional.empty(), result);
    }
}
