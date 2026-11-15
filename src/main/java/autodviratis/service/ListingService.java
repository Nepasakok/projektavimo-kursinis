package autodviratis.service;

import autodviratis.model.Listing;
import autodviratis.model.RawListing;
import autodviratis.model.SortOrder;
import autodviratis.model.Tenant;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ListingService {

    private static final String NO_DATA_MESSAGE = "Duomenų gauti nepavyko";
    private static final String LISTING_NOT_FOUND_MESSAGE = "Skelbimas nerastas";

    private final ListingNormalizer normalizer;
    private final DuplicateMerger merger;
    private final List<ListingSource> sources;
    private final ListingRepository repository;
    private final Clock clock;
    private final Map<String, List<SourceStatus>> lastStatuses = new ConcurrentHashMap<>();

    public ListingService(ListingNormalizer normalizer, DuplicateMerger merger, List<ListingSource> sources,
                          ListingRepository repository, Clock clock) {
        this.normalizer = normalizer;
        this.merger = merger;
        this.sources = sources;
        this.repository = repository;
        this.clock = clock;
    }

    public RefreshResult refresh(Tenant tenant) {
        List<SourceStatus> statuses = new ArrayList<>();
        for (ListingSource source : sources) {
            statuses.add(refreshSource(tenant, source));
        }
        List<SourceSnapshot> snapshots = new ArrayList<>(repository.findSnapshots(tenant));
        if (snapshots.isEmpty()) {
            lastStatuses.remove(tenant.getId());
            throw new NoDataAvailableException(NO_DATA_MESSAGE);
        }
        snapshots.sort(Comparator.comparing(SourceSnapshot::sourceName));
        List<Listing> merged = merger.merge(copyAllListings(snapshots));
        repository.replaceListings(tenant, merged);
        lastStatuses.put(tenant.getId(), List.copyOf(statuses));
        return new RefreshResult(statuses, merged.size());
    }

    public List<SourceStatus> getSourceStatuses(Tenant tenant) {
        return lastStatuses.getOrDefault(tenant.getId(), List.of());
    }

    public List<Listing> search(Tenant tenant, ListingFilter filter, SortOrder sortOrder) {
        return repository.search(tenant, filter, sortOrder);
    }

    public void hide(Tenant tenant, Long listingId) {
        Optional<Listing> listing = repository.findById(tenant, listingId);
        if (listing.isEmpty()) {
            throw new ListingNotFoundException(LISTING_NOT_FOUND_MESSAGE);
        }
        repository.delete(tenant, listingId);
    }

    private SourceStatus refreshSource(Tenant tenant, ListingSource source) {
        try {
            List<RawListing> rawListings = source.fetch(tenant);
            List<Listing> listings = normalizeAll(tenant, rawListings);
            Instant now = clock.instant();
            repository.saveSnapshot(tenant, new SourceSnapshot(source.getName(), listings, now));
            return SourceStatus.ok(source.getName(), toLocalTime(now), listings.size());
        } catch (SourceUnavailableException e) {
            Optional<SourceSnapshot> old = repository.findSnapshot(tenant, source.getName());
            if (old.isEmpty()) {
                return SourceStatus.unavailable(source.getName(), Optional.empty(), 0);
            }
            return SourceStatus.unavailable(source.getName(),
                    Optional.of(toLocalTime(old.get().lastUpdated())), old.get().listings().size());
        }
    }

    private List<Listing> normalizeAll(Tenant tenant, List<RawListing> rawListings) {
        List<Listing> listings = new ArrayList<>();
        for (RawListing raw : rawListings) {
            Optional<Listing> listing = normalizer.normalize(tenant, raw);
            if (listing.isPresent()) {
                listings.add(listing.get());
            }
        }
        return listings;
    }

    private List<Listing> copyAllListings(List<SourceSnapshot> snapshots) {
        List<Listing> copies = new ArrayList<>();
        for (SourceSnapshot snapshot : snapshots) {
            for (Listing listing : snapshot.listings()) {
                copies.add(listing.copy());
            }
        }
        return copies;
    }

    private LocalDateTime toLocalTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, clock.getZone());
    }
}
