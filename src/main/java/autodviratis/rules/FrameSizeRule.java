package autodviratis.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FrameSizeRule implements ParameterRule<String> {

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final Pattern LETTER_SIZE = Pattern.compile("(?<![\\p{L}\\d])(XS|S|M|L|XL)(?![\\p{L}\\d])");
    private static final Pattern CM_AFTER_WORD = Pattern.compile("(?:rėm|dyd)\\p{L}*\\s*:?\\s*(\\d{2})\\s*cm", FLAGS);
    private static final Pattern CM_BEFORE_WORD = Pattern.compile("(\\d{2})\\s*cm\\s*(?:rėm|dyd)", FLAGS);

    @Override
    public Optional<String> extract(String text) {
        List<String> sizes = new ArrayList<>();
        collect(LETTER_SIZE, text, "", sizes);
        collect(CM_AFTER_WORD, text, " cm", sizes);
        collect(CM_BEFORE_WORD, text, " cm", sizes);
        return SingleValue.pick(sizes);
    }

    private void collect(Pattern pattern, String text, String suffix, List<String> sizes) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            sizes.add(matcher.group(1) + suffix);
        }
    }
}
