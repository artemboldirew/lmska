package github.artemboldirew.lmska.core.service;

import github.artemboldirew.lmska.core.entity.*;
import java.util.List;
import java.util.UUID;

/**
 * Операции с категориями заданий
 */
public interface CategoryService {
    /**
     * Создаёт категорию с непустым уникальным названием
     */
    Category create(String name, String description);

    /**
     * Ищет категорию по UUID; возвращает null, если она не найдена
     */
    Category find(UUID id);

    /**
     * Возвращает все категории заданий
     */
    List<Category> findAll();

    /**
     * Меняет название и описание категории, сохраняя уникальность названия
     */
    Category update(UUID id, String name, String description);

    /**
     * Удаляет категорию, только если в ней нет заданий
     */
    void delete(UUID id);
}
