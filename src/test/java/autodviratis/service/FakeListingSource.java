package autodviratis.service;

import autodviratis.model.RawListing;
import autodviratis.model.Tenant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FakeListingSource implements ListingSource {

    private final String name;
    private final Map<String, List<RawListing>> listingsByTenant = new HashMap<>();
    private boolean available = true;

    FakeListingSource(String name) {
        this.name = name;
    }

    void setListings(Tenant tenant, List<RawListing> listings) {
        listingsByTenant.put(tenant.getId(), new ArrayList<>(listings));
    }

    void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<RawListing> fetch(Tenant tenant) throws SourceUnavailableException {
        if (!available) {
            throw new SourceUnavailableException(name + " nepasiekiamas");
        }
        return listingsByTenant.getOrDefault(tenant.getId(), List.of());
    }
}
