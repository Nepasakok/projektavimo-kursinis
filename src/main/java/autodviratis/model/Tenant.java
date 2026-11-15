package autodviratis.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Tenant {

    @Id
    private String id;
    private String name;

    protected Tenant() {
    }

    public Tenant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
