package autodviratis.rules;

import autodviratis.model.BrakeType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BrakeTypeRuleTest {

    private final BrakeTypeRule rule = new BrakeTypeRule();

    @Test
    void discBrakesAreFound() {
        Optional<BrakeType> result = rule.extract("Diskiniai stabdžiai");

        assertEquals(Optional.of(BrakeType.DISC), result);
    }

    @Test
    void hydraulicDiscBrakesAreHydraulic() {
        Optional<BrakeType> result = rule.extract("Hidrauliniai diskiniai stabdžiai");

        assertEquals(Optional.of(BrakeType.HYDRAULIC), result);
    }

    @Test
    void vBrakeIsFound() {
        Optional<BrakeType> result = rule.extract("Stabdžiai V-Brake");

        assertEquals(Optional.of(BrakeType.V_BRAKE), result);
    }

    @Test
    void discAndVBrakeTogetherAreNotSpecified() {
        Optional<BrakeType> result = rule.extract("Diskiniai priekyje, V-brake gale");

        assertEquals(Optional.empty(), result);
    }

    @Test
    void noBrakesMentionedIsNotSpecified() {
        Optional<BrakeType> result = rule.extract("Geras dviratis");

        assertEquals(Optional.empty(), result);
    }
}
