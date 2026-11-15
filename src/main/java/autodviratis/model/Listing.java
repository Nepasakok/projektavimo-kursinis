package autodviratis.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Listing {

    public static final double WHEEL_SIZE_NOT_SPECIFIED = 0;
    public static final int GEARS_NOT_SPECIFIED = 0;
    public static final String FRAME_SIZE_NOT_SPECIFIED = "";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Tenant tenant;
    private String title;
    private BigDecimal price;
    private String city;
    private String photoHash;
    @Enumerated(EnumType.STRING)
    private BikeType type;
    private double wheelSize;
    private String frameSize;
    @Enumerated(EnumType.STRING)
    private BrakeType brakeType;
    private int gears;
    @Enumerated(EnumType.STRING)
    private FrameMaterial frameMaterial;
    @ManyToOne
    private StoredSnapshot snapshot;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "listing_id")
    @OrderColumn(name = "link_order")
    private List<SourceLink> sourceLinks = new ArrayList<>();

    protected Listing() {
    }

    public Listing(Tenant tenant, String title, BigDecimal price, String city, String photoHash,
                   BikeType type, double wheelSize, String frameSize, BrakeType brakeType,
                   int gears, FrameMaterial frameMaterial, SourceLink sourceLink) {
        this.tenant = tenant;
        this.title = title;
        this.price = price;
        this.city = city;
        this.photoHash = photoHash;
        this.type = type;
        this.wheelSize = wheelSize;
        this.frameSize = frameSize;
        this.brakeType = brakeType;
        this.gears = gears;
        this.frameMaterial = frameMaterial;
        this.sourceLinks.add(sourceLink);
    }

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCity() {
        return city;
    }

    public String getPhotoHash() {
        return photoHash;
    }

    public BikeType getType() {
        return type;
    }

    public double getWheelSize() {
        return wheelSize;
    }

    public String getFrameSize() {
        return frameSize;
    }

    public BrakeType getBrakeType() {
        return brakeType;
    }

    public int getGears() {
        return gears;
    }

    public FrameMaterial getFrameMaterial() {
        return frameMaterial;
    }

    public List<SourceLink> getSourceLinks() {
        return sourceLinks;
    }

    public void addSourceLink(SourceLink sourceLink) {
        sourceLinks.add(sourceLink);
    }

    public Listing copy() {
        Listing copy = new Listing(tenant, title, price, city, photoHash, type, wheelSize, frameSize,
                brakeType, gears, frameMaterial, copyOf(sourceLinks.get(0)));
        for (int i = 1; i < sourceLinks.size(); i++) {
            copy.addSourceLink(copyOf(sourceLinks.get(i)));
        }
        return copy;
    }

    private SourceLink copyOf(SourceLink link) {
        return new SourceLink(link.getSourceName(), link.getUrl());
    }

    public void setSnapshot(StoredSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPhotoHash(String photoHash) {
        this.photoHash = photoHash;
    }

    public void setType(BikeType type) {
        this.type = type;
    }

    public void setWheelSize(double wheelSize) {
        this.wheelSize = wheelSize;
    }

    public void setFrameSize(String frameSize) {
        this.frameSize = frameSize;
    }

    public void setBrakeType(BrakeType brakeType) {
        this.brakeType = brakeType;
    }

    public void setGears(int gears) {
        this.gears = gears;
    }

    public void setFrameMaterial(FrameMaterial frameMaterial) {
        this.frameMaterial = frameMaterial;
    }
}
