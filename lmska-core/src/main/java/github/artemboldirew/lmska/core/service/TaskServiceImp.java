package github.artemboldirew.lmska.core.service;

import com.github.artemboldirew.lmska.bibernate.EntityManager;
import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.core.entity.Category;
import github.artemboldirew.lmska.core.entity.Task;
import java.util.List;
import java.util.UUID;

@Component
public class TaskServiceImp implements TaskService {
    private final EntityManager em;
    public TaskServiceImp(EntityManager em) {
        this.em = em;
    }

    public Task create(UUID categoryId, String title, String description) {
        validateTitle(title);
        validateDescription(description);
        Category category = em.find(Category.class, categoryId);
        if (category == null) throw new IllegalArgumentException("Unknown category: " + categoryId);
        Task task = new Task(UUID.randomUUID(), category, title, description);
        em.persist(task);
        em.flush();
        return task;
    }

    public Task find(UUID id) {
        return em.find(Task.class, id);
    }

    public List<Task> findAll() {
        return em.query("SELECT id FROM tasks").stream().map(row -> find((UUID) row.get("id"))).toList();
    }

    public List<Task> findByCategory(UUID categoryId) {
        return em.query("SELECT id FROM tasks WHERE category_id = ?", categoryId).stream()
                .map(row -> find((UUID) row.get("id"))).toList();
    }

    public Task update(UUID id, UUID categoryId, String title, String description) {
        Task task = require(id);
        validateTitle(title);
        validateDescription(description);
        Category category = em.find(Category.class, categoryId);
        if (category == null) throw new IllegalArgumentException("Unknown category: " + categoryId);
        task.setCategory(category);
        task.setTitle(title);
        task.setDescription(description);
        em.flush();
        return task;
    }

    public void delete(UUID id) {
        Task task = require(id);
        if (!em.query("SELECT id FROM task_attempts WHERE task_id = ?", id).isEmpty())
            throw new IllegalStateException("Task has attempts: " + id);
        em.remove(task);
        em.flush();
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Task title is required");
        if (title.length() > 255) throw new IllegalArgumentException("Название задания превышает 255 символов");
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > 255) throw new IllegalArgumentException("Описание превышает 255 символов");
    }

    private Task require(UUID id) {
        Task task = find(id);
        if (task == null) throw new IllegalArgumentException("Unknown task: " + id);
        return task;
    }
}
