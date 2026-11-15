package autodviratis.rules;

import autodviratis.model.FrameMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class FrameMaterialRule implements ParameterRule<FrameMaterial> {
    @Override
    public Optional<FrameMaterial> extract(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        List<FrameMaterial> found = new ArrayList<>();
        if (lower.contains("aliumin")) {
            found.add(FrameMaterial.ALUMINIUM);
        }
        if (lower.contains("plien")) {
            found.add(FrameMaterial.STEEL);
        }
        if (lower.contains("karbon")) {
            found.add(FrameMaterial.CARBON);
        }
        return SingleValue.pick(found);
    }
}
