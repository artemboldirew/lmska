package github.artemboldirew.lmska.core.entity;

import com.github.artemboldirew.lmska.bibernate.annotation.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    private UUID id;

    private String username;
    private String email;

    @OneToMany(mappedBy = "user")
    private List<TaskAttempt> attempts;

    public User() {
    }
    public User(UUID id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public List<TaskAttempt> getAttempts() {
        return attempts == null ? List.of() : attempts;
    }
}
