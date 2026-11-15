package autodviratis.rules;

import autodviratis.model.BrakeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class BrakeTypeRule implements ParameterRule<BrakeType> {

    @Override
    public Optional<BrakeType> extract(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        List<BrakeType> found = new ArrayList<>();
        if (lower.contains("hidraulin")) {
            found.add(BrakeType.HYDRAULIC);
        } else if (lower.contains("diskin") || lower.contains("diskai")) {
            found.add(BrakeType.DISC);
        }
        if (lower.contains("v-brake") || lower.contains("v brake") || lower.contains("vbrake")) {
            found.add(BrakeType.V_BRAKE);
        }
        return SingleValue.pick(found);
    }
}
