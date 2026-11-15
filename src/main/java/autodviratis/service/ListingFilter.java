package autodviratis.service;

import autodviratis.model.BikeType;
import autodviratis.model.Listing;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

public class ListingFilter {

    private final BikeType type;
    private final Optional<Double> wheelSize;
    private final Optional<BigDecimal> minPrice;
    private final Optional<BigDecimal> maxPrice;
    private final Optional<String> city;
    private final boolean includeUnspecified;

    public ListingFilter(BikeType type, Optional<Double> wheelSize, Optional<BigDecimal> minPrice,
                         Optional<BigDecimal> maxPrice, Optional<String> city, boolean includeUnspecified) {
        this.type = type;
        this.wheelSize = wheelSize;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.city = city;
        this.includeUnspecified = includeUnspecified;
    }

    public static ListingFilter noFilter() {
        return new ListingFilter(BikeType.NOT_SPECIFIED, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), false);
    }

    public BikeType getType() {
        return type;
    }

    public Optional<Double> getWheelSize() {
        return wheelSize;
    }

    public Optional<BigDecimal> getMinPrice() {
        return minPrice;
    }

    public Optional<BigDecimal> getMaxPrice() {
        return maxPrice;
    }

    public Optional<String> getCity() {
        return city;
    }

    public boolean isIncludeUnspecified() {
        return includeUnspecified;
    }

    public boolean matches(Listing listing) {
        return matchesType(listing) && matchesWheelSize(listing)
                && matchesPrice(listing) && matchesCity(listing);
    }

    private boolean matchesType(Listing listing) {
        if (type == BikeType.NOT_SPECIFIED) {
            return true;
        }
        if (listing.getType() == BikeType.NOT_SPECIFIED) {
            return includeUnspecified;
        }
        return listing.getType() == type;
    }

    private boolean matchesWheelSize(Listing listing) {
        if (wheelSize.isEmpty()) {
            return true;
        }
        if (listing.getWheelSize() == Listing.WHEEL_SIZE_NOT_SPECIFIED) {
            return includeUnspecified;
        }
        return listing.getWheelSize() == wheelSize.get();
    }

    private boolean matchesPrice(Listing listing) {
        if (minPrice.isPresent() && listing.getPrice().compareTo(minPrice.get()) < 0) {
            return false;
        }
        if (maxPrice.isPresent() && listing.getPrice().compareTo(maxPrice.get()) > 0) {
            return false;
        }
        return true;
    }

    private boolean matchesCity(Listing listing) {
        if (city.isEmpty()) {
            return true;
        }
        if (listing.getCity() == null || listing.getCity().isBlank()) {
            return includeUnspecified;
        }
        return listing.getCity().trim().toLowerCase(Locale.ROOT)
                .equals(city.get().trim().toLowerCase(Locale.ROOT));
    }
}
