package autodviratis.utils;

import java.math.BigDecimal;

public record JsonListing(String title, String description, BigDecimal price, String city,
                          String photoHash, String url) {
}
