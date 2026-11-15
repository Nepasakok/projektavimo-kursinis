package autodviratis.service;

import autodviratis.model.RawListing;
import autodviratis.model.Tenant;

import java.util.List;

public interface ListingSource {

    String getName();

    List<RawListing> fetch(Tenant tenant) throws SourceUnavailableException;
}
