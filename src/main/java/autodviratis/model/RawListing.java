package autodviratis.model;

import java.math.BigDecimal;

public record RawListing(String sourceName, String title, String description, BigDecimal price,
                         String city, String photoHash, String url) {

    public RawListing {
        if (title == null) {
            title = "";
        }
        if (description == null) {
            description = "";
        }
    }
}
