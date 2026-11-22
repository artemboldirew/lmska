package github.artemboldirew.lmska.core.service;

import com.github.artemboldirew.lmska.bibernate.EntityManager;
import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.core.entity.*;
import java.util.List;
import java.util.UUID;

@Component
public class TaskAttemptServiceImp implements TaskAttemptService {
    private final EntityManager em;
    public TaskAttemptServiceImp(EntityManager em) {
        this.em = em;
    }

    public TaskAttempt create(UUID userId, UUID taskId) {
        User user = em.find(User.class, userId);
        Task task = em.find(Task.class, taskId);
        if (user == null) throw new IllegalArgumentException("Unknown user: " + userId);
        if (task == null) throw new IllegalArgumentException("Unknown task: " + taskId);
        TaskAttempt attempt = new TaskAttempt(UUID.randomUUID(), user, task);
        em.persist(attempt);
        em.flush();
        return attempt;
    }

    public TaskAttempt find(UUID id) {
        return em.find(TaskAttempt.class, id);
    }

    public List<TaskAttempt> findAll() {
        return em.query("SELECT id FROM task_attempts").stream().map(row -> find((UUID) row.get("id"))).toList();
    }

    public List<TaskAttempt> findByUser(UUID userId) {
        return em.query("SELECT id FROM task_attempts WHERE user_id = ?", userId).stream()
                .map(row -> find((UUID) row.get("id"))).toList();
    }

    public List<TaskAttempt> findByTask(UUID taskId) {
        return em.query("SELECT id FROM task_attempts WHERE task_id = ?", taskId).stream()
                .map(row -> find((UUID) row.get("id"))).toList();
    }

    public TaskAttempt updateAnswer(UUID id, String answer) {
        TaskAttempt attempt = require(id);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) throw new IllegalStateException("Attempt already submitted: " + id);
        if (answer != null && answer.length() > 255) throw new IllegalArgumentException("Answer exceeds 255 characters");
        attempt.setAnswer(answer);
        em.flush();
        return attempt;
    }

    public TaskAttempt submit(UUID id, String answer) {
        if (answer == null || answer.isBlank()) throw new IllegalArgumentException("Ответ не должен быть пустым");
        TaskAttempt attempt = updateAnswer(id, answer);
        attempt.setStatus(AttemptStatus.SUBMITTED);
        em.flush();
        return attempt;
    }

    public TaskAttempt grade(UUID id, double score, boolean correct) {
        TaskAttempt attempt = require(id);
        if (attempt.getStatus() != AttemptStatus.SUBMITTED) throw new IllegalStateException("Attempt is not submitted: " + id);
        if (!Double.isFinite(score) || score < 0) throw new IllegalArgumentException("Invalid score: " + score);
        attempt.setScore(score);
        attempt.setStatus(correct ? AttemptStatus.CORRECT : AttemptStatus.INCORRECT);
        attempt.setCompletedAt(System.currentTimeMillis());
        em.flush();
        return attempt;
    }

    public void delete(UUID id) {
        em.remove(require(id));
        em.flush();
    }

    private TaskAttempt require(UUID id) {
        TaskAttempt attempt = find(id);
        if (attempt == null) throw new IllegalArgumentException("Unknown attempt: " + id);
        return attempt;
    }
}
