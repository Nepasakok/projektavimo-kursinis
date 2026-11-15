package autodviratis.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class StoredSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Tenant tenant;
    private String sourceName;
    private Instant lastUpdated;
    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Listing> listings = new ArrayList<>();

    protected StoredSnapshot() {
    }

    public StoredSnapshot(Tenant tenant, String sourceName, Instant lastUpdated, List<Listing> listings) {
        this.tenant = tenant;
        this.sourceName = sourceName;
        this.lastUpdated = lastUpdated;
        for (Listing listing : listings) {
            listing.setSnapshot(this);
            this.listings.add(listing);
        }
    }

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getSourceName() {
        return sourceName;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public List<Listing> getListings() {
        return listings;
    }
}
