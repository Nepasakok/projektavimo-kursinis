package autodviratis.rules;

import autodviratis.model.BikeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class BikeTypeRule implements ParameterRule<BikeType> {

    @Override
    public Optional<BikeType> extract(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        List<BikeType> found = new ArrayList<>();
        if (lower.contains("kaln") || lower.contains("mtb")) {
            found.add(BikeType.MOUNTAIN);
        }
        if (lower.contains("plent")) {
            found.add(BikeType.ROAD);
        }
        if (lower.contains("miest")) {
            found.add(BikeType.CITY);
        }
        if (lower.contains("vaikišk")) {
            found.add(BikeType.KIDS);
        }
        return SingleValue.pick(found);
    }
}
