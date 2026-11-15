package autodviratis.sources;

import autodviratis.model.RawListing;
import autodviratis.model.Tenant;
import autodviratis.service.ListingSource;
import autodviratis.service.SourceUnavailableException;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SwitchableListingSource implements ListingSource {

    private final ListingSource wrapped;
    private final Set<String> disabledTenantIds = ConcurrentHashMap.newKeySet();

    public SwitchableListingSource(ListingSource wrapped) {
        this.wrapped = wrapped;
    }

    public boolean isEnabled(Tenant tenant) {
        return !disabledTenantIds.contains(tenant.getId());
    }

    public void setEnabled(Tenant tenant, boolean enabled) {
        if (enabled) {
            disabledTenantIds.remove(tenant.getId());
        } else {
            disabledTenantIds.add(tenant.getId());
        }
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public List<RawListing> fetch(Tenant tenant) throws SourceUnavailableException {
        if (!isEnabled(tenant)) {
            throw new SourceUnavailableException(wrapped.getName() + " išjungtas");
        }
        return wrapped.fetch(tenant);
    }
}
