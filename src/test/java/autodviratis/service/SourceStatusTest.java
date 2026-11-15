package autodviratis.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceStatusTest {

    private final LocalDateTime time = LocalDateTime.of(2026, 11, 12, 14, 30);

    @Test
    void okMessageShowsTimeAndCount() {
        SourceStatus status = SourceStatus.ok("Skelbiu.lt", time, 3);

        assertEquals("Skelbiu.lt: duomenys atnaujinti 2026-11-12 14:30 (3 skelbimai)", status.message());
    }

    @Test
    void okMessageUsesSingularForOne() {
        SourceStatus status = SourceStatus.ok("Skelbiu.lt", time, 1);

        assertEquals("Skelbiu.lt: duomenys atnaujinti 2026-11-12 14:30 (1 skelbimas)", status.message());
    }

    @Test
    void okMessageUsesGenitivePluralForTen() {
        SourceStatus status = SourceStatus.ok("Skelbiu.lt", time, 10);

        assertEquals("Skelbiu.lt: duomenys atnaujinti 2026-11-12 14:30 (10 skelbimų)", status.message());
    }

    @Test
    void okMessageUsesGenitivePluralForEleven() {
        SourceStatus status = SourceStatus.ok("Skelbiu.lt", time, 11);

        assertEquals("Skelbiu.lt: duomenys atnaujinti 2026-11-12 14:30 (11 skelbimų)", status.message());
    }

    @Test
    void unavailableMessageWithOldDataShowsLastUpdate() {
        SourceStatus status = SourceStatus.unavailable("Skelbiu.lt", Optional.of(time), 3);

        assertEquals("Skelbiu.lt nepasiekiamas. Rodomi ankstesni duomenys, paskutinį kartą atnaujinti 2026-11-12 14:30.",
                status.message());
    }

    @Test
    void unavailableMessageWithoutOldDataSaysSo() {
        SourceStatus status = SourceStatus.unavailable("Skelbiu.lt", Optional.empty(), 0);

        assertEquals("Skelbiu.lt nepasiekiamas, ankstesnių duomenų nėra.", status.message());
    }
}
