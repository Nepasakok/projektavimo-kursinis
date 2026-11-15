package autodviratis.service;

import autodviratis.model.Listing;
import autodviratis.model.SortOrder;
import autodviratis.model.Tenant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class FakeListingRepository implements ListingRepository {

    private final Map<String, Map<String, SourceSnapshot>> snapshotsByTenant = new HashMap<>();
    private final Map<String, List<Listing>> listingsByTenant = new HashMap<>();
    private long nextId = 1;

    @Override
    public List<SourceSnapshot> findSnapshots(Tenant tenant) {
        Map<String, SourceSnapshot> snapshots = snapshotsByTenant.getOrDefault(tenant.getId(), Map.of());
        return new ArrayList<>(snapshots.values());
    }

    @Override
    public Optional<SourceSnapshot> findSnapshot(Tenant tenant, String sourceName) {
        Map<String, SourceSnapshot> snapshots = snapshotsByTenant.getOrDefault(tenant.getId(), Map.of());
        return Optional.ofNullable(snapshots.get(sourceName));
    }

    @Override
    public void saveSnapshot(Tenant tenant, SourceSnapshot snapshot) {
        snapshotsByTenant.computeIfAbsent(tenant.getId(), id -> new LinkedHashMap<>())
                .put(snapshot.sourceName(), snapshot);
    }

    @Override
    public void replaceListings(Tenant tenant, List<Listing> listings) {
        for (Listing listing : listings) {
            listing.setId(nextId);
            nextId++;
        }
        listingsByTenant.put(tenant.getId(), new ArrayList<>(listings));
    }

    @Override
    public List<Listing> search(Tenant tenant, ListingFilter filter, SortOrder sortOrder) {
        List<Listing> result = new ArrayList<>();
        for (Listing listing : listingsByTenant.getOrDefault(tenant.getId(), List.of())) {
            if (filter.matches(listing)) {
                result.add(listing);
            }
        }
        if (sortOrder == SortOrder.PRICE_ASC) {
            result.sort((a, b) -> a.getPrice().compareTo(b.getPrice()));
        }
        if (sortOrder == SortOrder.PRICE_DESC) {
            result.sort((a, b) -> b.getPrice().compareTo(a.getPrice()));
        }
        return result;
    }

    @Override
    public Optional<Listing> findById(Tenant tenant, Long listingId) {
        for (Listing listing : listingsByTenant.getOrDefault(tenant.getId(), List.of())) {
            if (listing.getId().equals(listingId)) {
                return Optional.of(listing);
            }
        }
        return Optional.empty();
    }

    @Override
    public void delete(Tenant tenant, Long listingId) {
        List<Listing> listings = listingsByTenant.getOrDefault(tenant.getId(), new ArrayList<>());
        for (int i = 0; i < listings.size(); i++) {
            if (listings.get(i).getId().equals(listingId)) {
                listings.remove(i);
                return;
            }
        }
    }
}
