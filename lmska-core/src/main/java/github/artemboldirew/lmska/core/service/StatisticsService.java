package github.artemboldirew.lmska.core.service;

import java.util.UUID;

/**
 * Рассчитывает статистику пользователя по его попыткам
 */
public interface StatisticsService {
    /**
     * Считает статистику пользователя
     * Если попыток нет, возвращает нули
     */
    UserStatistics forUser(UUID userId);

    /**
     * Результат подсчёта статистики, без отдельной таблицы
     */
    record UserStatistics(long total, long inProgress, long submitted, long correct, long incorrect,
                          double correctPercent, double averageScore) {
                          }
}
