package autodviratis.service;

import autodviratis.model.Listing;

import java.time.Instant;
import java.util.List;

public record SourceSnapshot(String sourceName, List<Listing> listings, Instant lastUpdated) {

    public SourceSnapshot {
        listings = List.copyOf(listings);
    }
}
