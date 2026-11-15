package autodviratis.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WheelSizeRule implements ParameterRule<Double> {

    public static final double MIN_WHEEL_SIZE = 12;
    public static final double MAX_WHEEL_SIZE = 29;

    private static final Pattern PATTERN = Pattern.compile(
            "(?<![\\d.,])(\\d{1,2}(?:[.,]\\d)?)\\s*(?:[\"“”″]|coli)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    @Override
    public Optional<Double> extract(String text) {
        List<Double> sizes = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            sizes.add(Double.parseDouble(matcher.group(1).replace(',', '.')));
        }
        Optional<Double> size = SingleValue.pick(sizes);
        if (size.isPresent() && size.get() >= MIN_WHEEL_SIZE && size.get() <= MAX_WHEEL_SIZE) {
            return size;
        }
        return Optional.empty();
    }
}
