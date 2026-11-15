package autodviratis.hibernate;

import autodviratis.model.Listing;
import autodviratis.model.SortOrder;
import autodviratis.model.StoredSnapshot;
import autodviratis.model.Tenant;
import autodviratis.service.ListingFilter;
import autodviratis.service.ListingRepository;
import autodviratis.service.SourceSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HibernateListingRepository implements ListingRepository {

    private final CustomHibernate customHibernate;

    public HibernateListingRepository(CustomHibernate customHibernate) {
        this.customHibernate = customHibernate;
    }

    @Override
    public List<SourceSnapshot> findSnapshots(Tenant tenant) {
        List<SourceSnapshot> snapshots = new ArrayList<>();
        for (StoredSnapshot stored : customHibernate.findSnapshots(tenant)) {
            snapshots.add(toSnapshot(stored));
        }
        return snapshots;
    }

    @Override
    public Optional<SourceSnapshot> findSnapshot(Tenant tenant, String sourceName) {
        Optional<StoredSnapshot> stored = customHibernate.findSnapshot(tenant, sourceName);
        if (stored.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toSnapshot(stored.get()));
    }

    @Override
    public void saveSnapshot(Tenant tenant, SourceSnapshot snapshot) {
        StoredSnapshot stored = new StoredSnapshot(tenant, snapshot.sourceName(), snapshot.lastUpdated(),
                snapshot.listings());
        customHibernate.replaceSnapshot(tenant, stored);
    }

    @Override
    public void replaceListings(Tenant tenant, List<Listing> listings) {
        customHibernate.replaceListings(tenant, listings);
    }

    @Override
    public List<Listing> search(Tenant tenant, ListingFilter filter, SortOrder sortOrder) {
        return customHibernate.searchListings(tenant, filter, sortOrder);
    }

    @Override
    public Optional<Listing> findById(Tenant tenant, Long listingId) {
        return customHibernate.findListing(tenant, listingId);
    }

    @Override
    public void delete(Tenant tenant, Long listingId) {
        customHibernate.deleteListing(tenant, listingId);
    }

    private SourceSnapshot toSnapshot(StoredSnapshot stored) {
        return new SourceSnapshot(stored.getSourceName(), stored.getListings(), stored.getLastUpdated());
    }
}
