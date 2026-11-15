package autodviratis.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import autodviratis.model.BikeType;
import autodviratis.model.Listing;
import autodviratis.model.SortOrder;
import autodviratis.model.StoredSnapshot;
import autodviratis.model.Tenant;
import autodviratis.service.ListingFilter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CustomHibernate {

    private final EntityManagerFactory entityManagerFactory;

    public CustomHibernate(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public List<Listing> searchListings(Tenant tenant, ListingFilter filter, SortOrder sortOrder) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Listing> query = cb.createQuery(Listing.class);
            Root<Listing> root = query.from(Listing.class);

            List<Predicate> predicates = currentListingPredicates(cb, root, tenant);
            addTypePredicate(cb, root, filter, predicates);
            addWheelSizePredicate(cb, root, filter, predicates);
            addPricePredicates(cb, root, filter, predicates);
            addCityPredicate(cb, root, filter, predicates);

            query.select(root).where(predicates.toArray(new Predicate[0]));
            if (sortOrder == SortOrder.PRICE_ASC) {
                query.orderBy(cb.asc(root.get("price")), cb.asc(root.get("id")));
            } else if (sortOrder == SortOrder.PRICE_DESC) {
                query.orderBy(cb.desc(root.get("price")), cb.asc(root.get("id")));
            } else {
                query.orderBy(cb.asc(root.get("id")));
            }
            return em.createQuery(query).getResultList();
        } finally {
            close(em);
        }
    }

    public Optional<Listing> findListing(Tenant tenant, Long listingId) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            return findCurrentListing(em, tenant, listingId);
        } finally {
            close(em);
        }
    }

    public void deleteListing(Tenant tenant, Long listingId) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            Optional<Listing> listing = findCurrentListing(em, tenant, listingId);
            if (listing.isPresent()) {
                em.remove(listing.get());
            }
            em.getTransaction().commit();
        } finally {
            close(em);
        }
    }

    public void replaceListings(Tenant tenant, List<Listing> listings) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Listing> query = cb.createQuery(Listing.class);
            Root<Listing> root = query.from(Listing.class);
            List<Predicate> predicates = currentListingPredicates(cb, root, tenant);
            query.select(root).where(predicates.toArray(new Predicate[0]));
            for (Listing old : em.createQuery(query).getResultList()) {
                em.remove(old);
            }
            for (Listing listing : listings) {
                em.persist(listing);
            }
            em.getTransaction().commit();
        } finally {
            close(em);
        }
    }

    public List<StoredSnapshot> findSnapshots(Tenant tenant) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            return findSnapshotsOf(em, tenant, Optional.empty());
        } finally {
            close(em);
        }
    }

    public Optional<StoredSnapshot> findSnapshot(Tenant tenant, String sourceName) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            List<StoredSnapshot> snapshots = findSnapshotsOf(em, tenant, Optional.of(sourceName));
            if (snapshots.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(snapshots.get(0));
        } finally {
            close(em);
        }
    }

    public void replaceSnapshot(Tenant tenant, StoredSnapshot snapshot) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            for (StoredSnapshot old : findSnapshotsOf(em, tenant, Optional.of(snapshot.getSourceName()))) {
                em.remove(old);
            }
            em.persist(snapshot);
            em.getTransaction().commit();
        } finally {
            close(em);
        }
    }

    private List<StoredSnapshot> findSnapshotsOf(EntityManager em, Tenant tenant, Optional<String> sourceName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<StoredSnapshot> query = cb.createQuery(StoredSnapshot.class);
        Root<StoredSnapshot> root = query.from(StoredSnapshot.class);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("tenant").get("id"), tenant.getId()));
        if (sourceName.isPresent()) {
            predicates.add(cb.equal(root.get("sourceName"), sourceName.get()));
        }
        query.select(root).where(predicates.toArray(new Predicate[0]));
        return em.createQuery(query).getResultList();
    }

    private Optional<Listing> findCurrentListing(EntityManager em, Tenant tenant, Long listingId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Listing> query = cb.createQuery(Listing.class);
        Root<Listing> root = query.from(Listing.class);
        List<Predicate> predicates = currentListingPredicates(cb, root, tenant);
        predicates.add(cb.equal(root.get("id"), listingId));
        query.select(root).where(predicates.toArray(new Predicate[0]));
        List<Listing> result = em.createQuery(query).getResultList();
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    private List<Predicate> currentListingPredicates(CriteriaBuilder cb, Root<Listing> root, Tenant tenant) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("tenant").get("id"), tenant.getId()));
        predicates.add(cb.isNull(root.get("snapshot")));
        return predicates;
    }

    private void addTypePredicate(CriteriaBuilder cb, Root<Listing> root, ListingFilter filter,
                                  List<Predicate> predicates) {
        if (filter.getType() == BikeType.NOT_SPECIFIED) {
            return;
        }
        Predicate sameType = cb.equal(root.get("type"), filter.getType());
        if (filter.isIncludeUnspecified()) {
            predicates.add(cb.or(sameType, cb.equal(root.get("type"), BikeType.NOT_SPECIFIED)));
        } else {
            predicates.add(sameType);
        }
    }

    private void addWheelSizePredicate(CriteriaBuilder cb, Root<Listing> root, ListingFilter filter,
                                       List<Predicate> predicates) {
        if (filter.getWheelSize().isEmpty()) {
            return;
        }
        Predicate sameSize = cb.equal(root.get("wheelSize"), filter.getWheelSize().get());
        if (filter.isIncludeUnspecified()) {
            Predicate unspecified = cb.equal(root.get("wheelSize"), Listing.WHEEL_SIZE_NOT_SPECIFIED);
            predicates.add(cb.or(sameSize, unspecified));
        } else {
            predicates.add(sameSize);
        }
    }

    private void addPricePredicates(CriteriaBuilder cb, Root<Listing> root, ListingFilter filter,
                                    List<Predicate> predicates) {
        if (filter.getMinPrice().isPresent()) {
            predicates.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("price"), filter.getMinPrice().get()));
        }
        if (filter.getMaxPrice().isPresent()) {
            predicates.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("price"), filter.getMaxPrice().get()));
        }
    }

    private void addCityPredicate(CriteriaBuilder cb, Root<Listing> root, ListingFilter filter,
                                  List<Predicate> predicates) {
        if (filter.getCity().isEmpty()) {
            return;
        }
        String city = filter.getCity().get().trim().toLowerCase(Locale.ROOT);
        Predicate sameCity = cb.equal(cb.lower(root.<String>get("city")), city);
        if (filter.isIncludeUnspecified()) {
            Predicate noCity = cb.or(cb.isNull(root.get("city")), cb.equal(cb.trim(root.<String>get("city")), ""));
            predicates.add(cb.or(sameCity, noCity));
        } else {
            predicates.add(sameCity);
        }
    }

    private void close(EntityManager em) {
        if (em == null) {
            return;
        }
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }
}
