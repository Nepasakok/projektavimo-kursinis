package autodviratis.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public record SourceStatus(String sourceName, State state, Optional<LocalDateTime> lastUpdated, int listingCount) {

    public enum State {
        OK,
        UNAVAILABLE
    }

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int LAST_DIGIT_DIVISOR = 10;
    private static final int LAST_TWO_DIGITS_DIVISOR = 100;
    private static final int TEENS_FROM = 11;
    private static final int TEENS_TO = 19;

    public static SourceStatus ok(String sourceName, LocalDateTime updated, int listingCount) {
        return new SourceStatus(sourceName, State.OK, Optional.of(updated), listingCount);
    }

    public static SourceStatus unavailable(String sourceName, Optional<LocalDateTime> lastUpdated, int listingCount) {
        return new SourceStatus(sourceName, State.UNAVAILABLE, lastUpdated, listingCount);
    }

    public String message() {
        if (state == State.OK) {
            return sourceName + ": duomenys atnaujinti " + formatTime()
                    + " (" + listingCount + " " + listingWord(listingCount) + ")";
        }
        if (lastUpdated.isPresent()) {
            return sourceName + " nepasiekiamas. Rodomi ankstesni duomenys, paskutinį kartą atnaujinti "
                    + formatTime() + ".";
        }
        return sourceName + " nepasiekiamas, ankstesnių duomenų nėra.";
    }

    private String formatTime() {
        return lastUpdated.orElseThrow().format(TIME_FORMAT);
    }

    private static String listingWord(int count) {
        int lastDigit = count % LAST_DIGIT_DIVISOR;
        int lastTwoDigits = count % LAST_TWO_DIGITS_DIVISOR;
        if (lastDigit == 0 || (lastTwoDigits >= TEENS_FROM && lastTwoDigits <= TEENS_TO)) {
            return "skelbimų";
        }
        if (lastDigit == 1) {
            return "skelbimas";
        }
        return "skelbimai";
    }
}
