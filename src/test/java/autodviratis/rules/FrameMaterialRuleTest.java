package autodviratis.rules;

import autodviratis.model.FrameMaterial;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameMaterialRuleTest {

    private final FrameMaterialRule rule = new FrameMaterialRule();

    @Test
    void aluminiumIsFound() {
        Optional<FrameMaterial> result = rule.extract("Aliuminis rėmas");

        assertEquals(Optional.of(FrameMaterial.ALUMINIUM), result);
    }

    @Test
    void steelIsFound() {
        Optional<FrameMaterial> result = rule.extract("Plieninis rėmas");

        assertEquals(Optional.of(FrameMaterial.STEEL), result);
    }

    @Test
    void carbonIsFound() {
        Optional<FrameMaterial> result = rule.extract("Karboninis rėmas");

        assertEquals(Optional.of(FrameMaterial.CARBON), result);
    }

    @Test
    void twoMaterialsAreNotSpecified() {
        Optional<FrameMaterial> result = rule.extract("Aliuminis rėmas, plieninė šakė");

        assertEquals(Optional.empty(), result);
    }

    @Test
    void noMaterialIsNotSpecified() {
        Optional<FrameMaterial> result = rule.extract("Geras dviratis");

        assertEquals(Optional.empty(), result);
    }
}
