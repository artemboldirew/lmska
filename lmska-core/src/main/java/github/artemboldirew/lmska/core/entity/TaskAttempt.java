package github.artemboldirew.lmska.core.entity;

import com.github.artemboldirew.lmska.bibernate.annotation.*;
import java.util.UUID;

@Entity
@Table(name = "task_attempts")
public class TaskAttempt {
    @Id
    private UUID id;

    @ManyToOne(joinColumn = "user_id")
    private User user;

    @ManyToOne(joinColumn = "task_id")
    private Task task;

    private String answer;
    private String status;
    private Double score;
    private Long startedAt;
    private Long completedAt;

    public TaskAttempt() {
    }
    public TaskAttempt(UUID id, User user, Task task) {
        this.id = id;
        this.user = user;
        this.task = task;
        this.status = AttemptStatus.IN_PROGRESS.name();
        this.startedAt = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public Task getTask() {
        return task;
    }
    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public AttemptStatus getStatus() {
        return AttemptStatus.valueOf(status);
    }
    public void setStatus(AttemptStatus status) {
        this.status = status.name();
    }
    public Double getScore() {
        return score;
    }
    public void setScore(Double score) {
        this.score = score;
    }
    public Long getStartedAt() {
        return startedAt;
    }
    public Long getCompletedAt() {
        return completedAt;
    }
    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }
}
