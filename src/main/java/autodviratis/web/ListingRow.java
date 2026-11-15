package autodviratis.web;

import autodviratis.model.Listing;
import autodviratis.model.SourceLink;

import java.util.List;

public record ListingRow(Long id, String title, String price, String city, String type, String wheelSize,
                         String frameSize, String brakeType, String gears, String frameMaterial,
                         List<SourceLink> links) {

    public static ListingRow from(Listing listing) {
        String city = listing.getCity();
        if (city == null || city.isBlank()) {
            city = Labels.NOT_SPECIFIED;
        }
        return new ListingRow(
                listing.getId(),
                listing.getTitle(),
                Labels.price(listing.getPrice()),
                city,
                Labels.type(listing.getType()),
                Labels.wheelSize(listing.getWheelSize()),
                Labels.frameSize(listing.getFrameSize()),
                Labels.brake(listing.getBrakeType()),
                Labels.gears(listing.getGears()),
                Labels.material(listing.getFrameMaterial()),
                listing.getSourceLinks());
    }
}
