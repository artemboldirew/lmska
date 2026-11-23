package github.artemboldirew.lmska.core.service;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.core.entity.TaskAttempt;
import java.util.UUID;

@Component
public class StatisticsServiceImp implements StatisticsService {
    private final UserService users;
    private final TaskAttemptService attempts;

    public StatisticsServiceImp(UserService users, TaskAttemptService attempts) {
        this.users = users;
        this.attempts = attempts;
    }

    @Override
    public UserStatistics forUser(UUID userId) {
        if (users.find(userId) == null) throw new IllegalArgumentException("Пользователь не найден: " + userId);
        long inProgress = 0, submitted = 0, correct = 0, incorrect = 0;
        double average = 0;
        long graded = 0;
        var records = attempts.findByUser(userId);
        for (TaskAttempt attempt : records) {
            switch (attempt.getStatus()) {
                case IN_PROGRESS -> inProgress++;
                case SUBMITTED -> submitted++;
                case CORRECT, INCORRECT -> {
                    if (attempt.getStatus() == github.artemboldirew.lmska.core.entity.AttemptStatus.CORRECT) correct++;
                    else incorrect++;
                    graded++;
                    average = average * ((graded - 1.0) / graded) + attempt.getScore() / graded;
                }
            }
        }
        return new UserStatistics(records.size(), inProgress, submitted, correct, incorrect,
                graded == 0 ? 0 : 100.0 * correct / graded, average);
    }
}
