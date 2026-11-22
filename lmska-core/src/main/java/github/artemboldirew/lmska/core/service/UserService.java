package github.artemboldirew.lmska.core.service;

import github.artemboldirew.lmska.core.entity.*;
import java.util.List;
import java.util.UUID;

/**
 * Операции с пользователями
 */
public interface UserService {
    /**
     * Создаёт пользователя, проверяя имя, email и длину полей
     */
    User create(String username, String email);

    /**
     * Ищет пользователя по UUID; возвращает null, если он не найден
     */
    User find(UUID id);

    /**
     * Возвращает всех пользователей
     */
    List<User> findAll();

    /**
     * Обновляет имя и email пользователя после проверки новых значений
     */
    User update(UUID id, String username, String email);

    /**
     * Удаляет пользователя, только если у него нет попыток
     */
    void delete(UUID id);
}
