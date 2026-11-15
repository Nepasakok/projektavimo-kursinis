package autodviratis.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GearsRule implements ParameterRule<Integer> {

    public static final int MIN_GEARS = 1;
    public static final int MAX_GEARS = 36;

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final Pattern NUMBER_BEFORE_WORD = Pattern.compile("(?<!\\d)(\\d{1,2})[\\s-]*(?:pavar|bėg)", FLAGS);
    private static final Pattern NUMBER_AFTER_WORD = Pattern.compile("(?:pavar|bėg)\\p{L}*\\s*:\\s*(\\d{1,2})", FLAGS);

    @Override
    public Optional<Integer> extract(String text) {
        List<Integer> gears = new ArrayList<>();
        collect(NUMBER_BEFORE_WORD, text, gears);
        collect(NUMBER_AFTER_WORD, text, gears);
        Optional<Integer> value = SingleValue.pick(gears);
        if (value.isPresent() && value.get() >= MIN_GEARS && value.get() <= MAX_GEARS) {
            return value;
        }
        return Optional.empty();
    }

    private void collect(Pattern pattern, String text, List<Integer> gears) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            gears.add(Integer.parseInt(matcher.group(1)));
        }
    }
}
