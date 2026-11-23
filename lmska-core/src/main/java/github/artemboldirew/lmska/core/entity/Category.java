package github.artemboldirew.lmska.core.entity;

import com.github.artemboldirew.lmska.bibernate.annotation.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    private UUID id;
    private String name;
    private String description;
    @OneToMany(mappedBy = "category")
    private List<Task> tasks;

    public Category() {
    }
    public Category(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public List<Task> getTasks() {
        return tasks == null ? List.of() : tasks;
    }
}
