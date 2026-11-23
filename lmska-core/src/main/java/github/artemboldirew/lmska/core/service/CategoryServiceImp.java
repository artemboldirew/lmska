package github.artemboldirew.lmska.core.service;

import com.github.artemboldirew.lmska.bibernate.EntityManager;
import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.core.entity.Category;
import java.util.List;
import java.util.UUID;

@Component
public class CategoryServiceImp implements CategoryService {
    private final EntityManager em;
    public CategoryServiceImp(EntityManager em) {
        this.em = em;
    }

    public Category create(String name, String description) {
        validateName(name, null);
        validateDescription(description);
        Category category = new Category(UUID.randomUUID(), name, description);
        em.persist(category);
        em.flush();
        return category;
    }

    public Category find(UUID id) {
        return em.find(Category.class, id);
    }

    public List<Category> findAll() {
        return em.query("SELECT id FROM categories").stream().map(row -> find((UUID) row.get("id"))).toList();
    }

    public Category update(UUID id, String name, String description) {
        Category category = require(id);
        validateName(name, id);
        validateDescription(description);
        category.setName(name);
        category.setDescription(description);
        em.flush();
        return category;
    }

    public void delete(UUID id) {
        Category category = require(id);
        if (!em.query("SELECT id FROM tasks WHERE category_id = ?", id).isEmpty())
            throw new IllegalStateException("Category has tasks: " + id);
        em.remove(category);
        em.flush();
    }

    private void validateName(String name, UUID currentId) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name is required");
        if (name.length() > 255) throw new IllegalArgumentException("Название категории превышает 255 символов");
        boolean duplicate = em.query("SELECT id FROM categories WHERE name = ?", name).stream()
                .anyMatch(row -> !row.get("id").equals(currentId));
        if (duplicate) throw new IllegalArgumentException("Category already exists: " + name);
    }

    private Category require(UUID id) {
        Category category = find(id);
        if (category == null) throw new IllegalArgumentException("Unknown category: " + id);
        return category;
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > 255) throw new IllegalArgumentException("Описание превышает 255 символов");
    }
}
