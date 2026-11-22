package github.artemboldirew.lmska.core.service;

import github.artemboldirew.lmska.core.entity.*;
import java.util.List;
import java.util.UUID;

/**
 * Операции с попытками выполнения заданий
 */
public interface TaskAttemptService {
    /**
     * Начинает попытку существующего пользователя для существующего задания
     */
    TaskAttempt create(UUID userId, UUID taskId);

    /**
     * Ищет попытку по UUID; возвращает null, если она не найдена
     */
    TaskAttempt find(UUID id);

    /**
     * Возвращает все попытки выполнения заданий
     */
    List<TaskAttempt> findAll();

    /**
     * Возвращает попытки пользователя
     */
    List<TaskAttempt> findByUser(UUID userId);

    /**
     * Возвращает попытки задания
     */
    List<TaskAttempt> findByTask(UUID taskId);

    /**
     * Сохраняет ответ только для начатой попытки
     */
    TaskAttempt updateAnswer(UUID id, String answer);

    /**
     * Отправляет ответ и переводит начатую попытку на проверку
     */
    TaskAttempt submit(UUID id, String answer);

    /**
     * Сохраняет ручную оценку только отправленной попытки
     */
    TaskAttempt grade(UUID id, double score, boolean correct);

    /**
     * Удаляет попытку независимо от её статуса
     */
    void delete(UUID id);
}
