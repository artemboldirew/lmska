package github.artemboldirew.lmska.core.service;

import github.artemboldirew.lmska.core.entity.*;
import java.util.List;
import java.util.UUID;

/**
 * Операции с заданиями
 */
public interface TaskService {
    /**
     * Создаёт задание с непустым названием в существующей категории
     */
    Task create(UUID categoryId, String title, String description);

    /**
     * Ищет задание по UUID; возвращает null, если оно не найдено
     */
    Task find(UUID id);

    /**
     * Возвращает все задания
     */
    List<Task> findAll();

    /**
     * Возвращает задания категории
     */
    List<Task> findByCategory(UUID categoryId);

    /**
     * Обновляет задание и проверяет существование выбранной категории
     */
    Task update(UUID id, UUID categoryId, String title, String description);

    /**
     * Удаляет задание, только если по нему нет попыток
     */
    void delete(UUID id);
}
