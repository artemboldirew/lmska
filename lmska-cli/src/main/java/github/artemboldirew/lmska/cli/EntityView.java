package github.artemboldirew.lmska.cli;

import github.artemboldirew.lmska.core.entity.*;

/**
 * Готовит данные для вывода в консоль
 */
public final class EntityView {
    private EntityView() {
    }

    public static String user(User value) {
        return value.getUsername() + " | Email: " + text(value.getEmail()) + " | ID: " + value.getId();
    }

    public static String category(Category value) {
        return value.getName() + " | " + text(value.getDescription()) + " | ID: " + value.getId();
    }

    public static String task(Task value) {
        return value.getTitle() + " | Категория: " + value.getCategory().getName()
                + " | Описание: " + text(value.getDescription()) + " | ID: " + value.getId();
    }

    public static String attempt(TaskAttempt value) {
        return "ID: " + value.getId() + " | Пользователь: " + value.getUser().getUsername()
                + " | Задание: " + value.getTask().getTitle() + " | Статус: " + value.getStatus()
                + " | Ответ: " + text(value.getAnswer()) + " | Оценка: " + text(value.getScore())
                + " | Начата: " + java.time.Instant.ofEpochMilli(value.getStartedAt())
                + " | Завершена: " + (value.getCompletedAt() == null ? "—" : java.time.Instant.ofEpochMilli(value.getCompletedAt()));
    }

    private static String text(Object value) {
        return value == null ? "—" : value.toString();
    }
}
