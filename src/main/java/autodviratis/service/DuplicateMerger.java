package autodviratis.service;

import autodviratis.model.BikeType;
import autodviratis.model.BrakeType;
import autodviratis.model.FrameMaterial;
import autodviratis.model.Listing;
import autodviratis.model.SourceLink;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class DuplicateMerger {

    public static final double MIN_TITLE_SIMILARITY = 0.80;
    public static final BigDecimal MAX_PRICE_DIFFERENCE = new BigDecimal("0.10");
    private static final Set<String> IGNORED_WORDS = Set.of("dviratis", "parduodu");

    public List<Listing> merge(List<Listing> listings) {
        List<Listing> result = new ArrayList<>();
        for (Listing listing : listings) {
            Optional<Listing> duplicate = findDuplicate(result, listing);
            if (duplicate.isPresent()) {
                mergeInto(duplicate.get(), listing);
            } else {
                result.add(listing);
            }
        }
        return result;
    }

    public boolean isDuplicate(Listing first, Listing second) {
        if (!first.getTenant().getId().equals(second.getTenant().getId())) {
            return false;
        }
        if (shareSource(first, second)) {
            return false;
        }
        if (!sameCity(first, second)) {
            return false;
        }
        if (hasPhoto(first) && hasPhoto(second)) {
            return first.getPhotoHash().equals(second.getPhotoHash());
        }
        return titleSimilarity(first.getTitle(), second.getTitle()) >= MIN_TITLE_SIMILARITY
                && pricesAreClose(first.getPrice(), second.getPrice());
    }

    private Optional<Listing> findDuplicate(List<Listing> merged, Listing listing) {
        for (Listing existing : merged) {
            if (isDuplicate(existing, listing)) {
                return Optional.of(existing);
            }
        }
        return Optional.empty();
    }

    private boolean shareSource(Listing first, Listing second) {
        for (SourceLink firstLink : first.getSourceLinks()) {
            for (SourceLink secondLink : second.getSourceLinks()) {
                if (firstLink.getSourceName().equals(secondLink.getSourceName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean sameCity(Listing first, Listing second) {
        if (first.getCity() == null || first.getCity().isBlank()) {
            return false;
        }
        if (second.getCity() == null || second.getCity().isBlank()) {
            return false;
        }
        return first.getCity().trim().toLowerCase(Locale.ROOT)
                .equals(second.getCity().trim().toLowerCase(Locale.ROOT));
    }

    private boolean hasPhoto(Listing listing) {
        return listing.getPhotoHash() != null && !listing.getPhotoHash().isBlank();
    }

    private double titleSimilarity(String firstTitle, String secondTitle) {
        Set<String> firstWords = titleWords(firstTitle);
        Set<String> secondWords = titleWords(secondTitle);
        int common = 0;
        for (String word : firstWords) {
            if (secondWords.contains(word)) {
                common++;
            }
        }
        int all = firstWords.size() + secondWords.size() - common;
        if (all == 0) {
            return 0;
        }
        return (double) common / all;
    }

    private Set<String> titleWords(String title) {
        String cleaned = title.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
        Set<String> words = new HashSet<>();
        if (cleaned.isEmpty()) {
            return words;
        }
        for (String word : cleaned.split(" ")) {
            if (!IGNORED_WORDS.contains(word)) {
                words.add(word);
            }
        }
        return words;
    }

    private boolean pricesAreClose(BigDecimal firstPrice, BigDecimal secondPrice) {
        BigDecimal difference = firstPrice.subtract(secondPrice).abs();
        BigDecimal allowed = firstPrice.min(secondPrice).multiply(MAX_PRICE_DIFFERENCE);
        return difference.compareTo(allowed) <= 0;
    }

    private void mergeInto(Listing target, Listing other) {
        if (other.getPrice().compareTo(target.getPrice()) < 0) {
            target.setPrice(other.getPrice());
        }
        for (SourceLink link : other.getSourceLinks()) {
            target.addSourceLink(link);
        }
        fillUnspecifiedFields(target, other);
    }

    private void fillUnspecifiedFields(Listing target, Listing other) {
        if (!hasPhoto(target)) {
            target.setPhotoHash(other.getPhotoHash());
        }
        if (target.getType() == BikeType.NOT_SPECIFIED) {
            target.setType(other.getType());
        }
        if (target.getWheelSize() == Listing.WHEEL_SIZE_NOT_SPECIFIED) {
            target.setWheelSize(other.getWheelSize());
        }
        if (target.getFrameSize().equals(Listing.FRAME_SIZE_NOT_SPECIFIED)) {
            target.setFrameSize(other.getFrameSize());
        }
        if (target.getBrakeType() == BrakeType.NOT_SPECIFIED) {
            target.setBrakeType(other.getBrakeType());
        }
        if (target.getGears() == Listing.GEARS_NOT_SPECIFIED) {
            target.setGears(other.getGears());
        }
        if (target.getFrameMaterial() == FrameMaterial.NOT_SPECIFIED) {
            target.setFrameMaterial(other.getFrameMaterial());
        }
    }
}
