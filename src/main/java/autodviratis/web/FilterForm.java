package autodviratis.web;

import autodviratis.model.BikeType;
import autodviratis.model.SortOrder;
import autodviratis.service.ListingFilter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FilterForm {

    private String type = "";
    private String wheelSize = "";
    private String minPrice = "";
    private String maxPrice = "";
    private String city = "";
    private boolean includeUnspecified;
    private String sort = "";

    public ListingFilter toFilter() {
        return new ListingFilter(parseType(), parseNumber(wheelSize).map(BigDecimal::doubleValue),
                parseNumber(minPrice), parseNumber(maxPrice), parseCity(), includeUnspecified);
    }

    public SortOrder toSortOrder() {
        for (SortOrder order : SortOrder.values()) {
            if (order != SortOrder.NOT_SPECIFIED && order.name().equals(sort)) {
                return order;
            }
        }
        return SortOrder.NOT_SPECIFIED;
    }

    public List<String> invalidFields() {
        List<String> invalid = new ArrayList<>();
        if (isInvalidNumber(wheelSize)) {
            invalid.add("Ratų dydis");
        }
        if (isInvalidNumber(minPrice)) {
            invalid.add("Kaina nuo");
        }
        if (isInvalidNumber(maxPrice)) {
            invalid.add("Kaina iki");
        }
        return invalid;
    }

    private BikeType parseType() {
        for (BikeType bikeType : BikeType.values()) {
            if (bikeType != BikeType.NOT_SPECIFIED && bikeType.name().equals(type)) {
                return bikeType;
            }
        }
        return BikeType.NOT_SPECIFIED;
    }

    private Optional<String> parseCity() {
        if (city == null || city.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(city.trim());
    }

    private Optional<BigDecimal> parseNumber(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(text.trim().replace(',', '.')));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private boolean isInvalidNumber(String text) {
        return text != null && !text.isBlank() && parseNumber(text).isEmpty();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWheelSize() {
        return wheelSize;
    }

    public void setWheelSize(String wheelSize) {
        this.wheelSize = wheelSize;
    }

    public String getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(String minPrice) {
        this.minPrice = minPrice;
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isIncludeUnspecified() {
        return includeUnspecified;
    }

    public void setIncludeUnspecified(boolean includeUnspecified) {
        this.includeUnspecified = includeUnspecified;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
