package autodviratis.service;

import autodviratis.model.BikeType;
import autodviratis.model.BrakeType;
import autodviratis.model.FrameMaterial;
import autodviratis.model.Listing;
import autodviratis.model.RawListing;
import autodviratis.model.SourceLink;
import autodviratis.model.Tenant;
import autodviratis.rules.ConflictResolver;
import autodviratis.rules.ParameterRule;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ListingNormalizer {

    private static final List<String> BIKE_WORDS = List.of("dvirat", "dvirač");
    private static final List<String> BIKE_NOMINATIVE_WORDS = List.of("dviratis", "dviračiai");
    private static final List<String> NOT_BIKE_WORDS =
            List.of("padang", "laikikl", "šalm", "dalys", "dalis", "pedal", "krepš");
    private final ParameterRule<BikeType> bikeTypeRule;
    private final ParameterRule<Double> wheelSizeRule;
    private final ParameterRule<String> frameSizeRule;
    private final ParameterRule<BrakeType> brakeTypeRule;
    private final ParameterRule<Integer> gearsRule;
    private final ParameterRule<FrameMaterial> frameMaterialRule;
    private final ConflictResolver conflictResolver;

    public ListingNormalizer(ParameterRule<BikeType> bikeTypeRule,
                             ParameterRule<Double> wheelSizeRule,
                             ParameterRule<String> frameSizeRule,
                             ParameterRule<BrakeType> brakeTypeRule,
                             ParameterRule<Integer> gearsRule,
                             ParameterRule<FrameMaterial> frameMaterialRule,
                             ConflictResolver conflictResolver) {
        this.bikeTypeRule = bikeTypeRule;
        this.wheelSizeRule = wheelSizeRule;
        this.frameSizeRule = frameSizeRule;
        this.brakeTypeRule = brakeTypeRule;
        this.gearsRule = gearsRule;
        this.frameMaterialRule = frameMaterialRule;
        this.conflictResolver = conflictResolver;
    }

    public Optional<Listing> normalize(Tenant tenant, RawListing raw) {
        Optional<BikeType> type = extract(bikeTypeRule, raw);
        if (!isBike(raw, type)) {
            return Optional.empty();
        }
        Listing listing = new Listing(
                tenant,
                raw.title(),
                raw.price(),
                raw.city(),
                raw.photoHash(),
                type.orElse(BikeType.NOT_SPECIFIED),
                extract(wheelSizeRule, raw).orElse(Listing.WHEEL_SIZE_NOT_SPECIFIED),
                extract(frameSizeRule, raw).orElse(Listing.FRAME_SIZE_NOT_SPECIFIED),
                extract(brakeTypeRule, raw).orElse(BrakeType.NOT_SPECIFIED),
                extract(gearsRule, raw).orElse(Listing.GEARS_NOT_SPECIFIED),
                extract(frameMaterialRule, raw).orElse(FrameMaterial.NOT_SPECIFIED),
                new SourceLink(raw.sourceName(), raw.url()));
        return Optional.of(listing);
    }

    private <T> Optional<T> extract(ParameterRule<T> rule, RawListing raw) {
        Optional<T> fromTitle = rule.extract(raw.title());
        Optional<T> fromDescription = rule.extract(raw.description());
        return conflictResolver.resolve(fromTitle, fromDescription);
    }

    private boolean hasBikeAsMainNoun(String lowerTitle) {
        for (String noun : BIKE_NOMINATIVE_WORDS) {
            if (lowerTitle.contains(noun)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBike(RawListing raw, Optional<BikeType> type) {
        String title = raw.title().toLowerCase(Locale.ROOT);
        if (!hasBikeAsMainNoun(title)) {
            for (String word : NOT_BIKE_WORDS) {
                if (title.contains(word)) {
                    return false;
                }
            }
        }
        if (type.isPresent()) {
            return true;
        }
        String text = title + " " + raw.description().toLowerCase(Locale.ROOT);
        for (String word : BIKE_WORDS) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
