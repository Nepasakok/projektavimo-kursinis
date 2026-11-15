package autodviratis.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class SourceLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceName;
    private String url;

    protected SourceLink() {
    }

    public SourceLink(String sourceName, String url) {
        this.sourceName = sourceName;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getUrl() {
        return url;
    }
}
