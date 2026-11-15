package autodviratis.service;

import autodviratis.model.Listing;
import autodviratis.model.SortOrder;
import autodviratis.model.Tenant;

import java.util.List;
import java.util.Optional;

public interface ListingRepository {

    List<SourceSnapshot> findSnapshots(Tenant tenant);

    Optional<SourceSnapshot> findSnapshot(Tenant tenant, String sourceName);

    void saveSnapshot(Tenant tenant, SourceSnapshot snapshot);

    void replaceListings(Tenant tenant, List<Listing> listings);

    List<Listing> search(Tenant tenant, ListingFilter filter, SortOrder sortOrder);

    Optional<Listing> findById(Tenant tenant, Long listingId);

    void delete(Tenant tenant, Long listingId);
}
