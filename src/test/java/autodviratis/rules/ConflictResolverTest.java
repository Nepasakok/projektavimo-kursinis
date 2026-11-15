package autodviratis.rules;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConflictResolverTest {

    private final ConflictResolver resolver = new ConflictResolver();

    @Test
    void titleAndDescriptionDisagreeIsNotSpecified() {
        Optional<Double> result = resolver.resolve(Optional.of(29.0), Optional.of(27.5));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void titleAndDescriptionAgreeKeepsValue() {
        Optional<Double> result = resolver.resolve(Optional.of(29.0), Optional.of(29.0));

        assertEquals(Optional.of(29.0), result);
    }

    @Test
    void onlyTitleHasValueKeepsIt() {
        Optional<Double> result = resolver.resolve(Optional.of(29.0), Optional.empty());

        assertEquals(Optional.of(29.0), result);
    }

    @Test
    void onlyDescriptionHasValueKeepsIt() {
        Optional<Double> result = resolver.resolve(Optional.empty(), Optional.of(27.5));

        assertEquals(Optional.of(27.5), result);
    }

    @Test
    void bothEmptyIsNotSpecified() {
        Optional<Double> result = resolver.resolve(Optional.empty(), Optional.empty());

        assertEquals(Optional.empty(), result);
    }
}
