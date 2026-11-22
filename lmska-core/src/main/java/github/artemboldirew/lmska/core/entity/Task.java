package github.artemboldirew.lmska.core.entity;

import com.github.artemboldirew.lmska.bibernate.annotation.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    private UUID id;

    @ManyToOne(joinColumn = "category_id")
    private Category category;

    private String title;
    private String description;

    @OneToMany(mappedBy = "task")
    private List<TaskAttempt> attempts;

    public Task() {
    }
    public Task(UUID id, Category category, String title, String description) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public List<TaskAttempt> getAttempts() {
        return attempts == null ? List.of() : attempts;
    }
}
