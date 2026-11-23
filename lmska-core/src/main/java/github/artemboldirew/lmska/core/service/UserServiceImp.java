package github.artemboldirew.lmska.core.service;

import com.github.artemboldirew.lmska.bibernate.EntityManager;
import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.core.entity.User;
import java.util.List;
import java.util.UUID;

@Component
public class UserServiceImp implements UserService {
    private final EntityManager em;
    public UserServiceImp(EntityManager em) {
        this.em = em;
    }

    public User create(String username, String email) {
        validateUsername(username);
        validateEmail(email);
        User user = new User(UUID.randomUUID(), username, email);
        em.persist(user);
        em.flush();
        return user;
    }

    public User find(UUID id) {
        return em.find(User.class, id);
    }

    public List<User> findAll() {
        return em.query("SELECT id FROM users").stream().map(row -> find((UUID) row.get("id"))).toList();
    }

    public User update(UUID id, String username, String email) {
        User user = require(id);
        validateUsername(username);
        validateEmail(email);
        user.setUsername(username);
        user.setEmail(email);
        em.flush();
        return user;
    }

    public void delete(UUID id) {
        User user = require(id);
        if (!em.query("SELECT id FROM task_attempts WHERE user_id = ?", id).isEmpty())
            throw new IllegalStateException("User has attempts: " + id);
        em.remove(user);
        em.flush();
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (username.length() > 255) throw new IllegalArgumentException("Имя пользователя превышает 255 символов");
    }

    private static void validateEmail(String email) {
        if (email != null && email.length() > 255) throw new IllegalArgumentException("Email превышает 255 символов");
        if (email != null && !email.isBlank() && !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Неверный формат email");
        }
    }

    private User require(UUID id) {
        User user = find(id);
        if (user == null) throw new IllegalArgumentException("Unknown user: " + id);
        return user;
    }
}
