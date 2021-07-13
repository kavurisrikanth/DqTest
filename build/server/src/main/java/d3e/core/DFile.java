package d3e.core;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DFile {

    private String name;
    @Id
    private String id;
    private long size;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
