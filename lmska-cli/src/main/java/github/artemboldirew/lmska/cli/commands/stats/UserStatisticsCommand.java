package github.artemboldirew.lmska.cli.commands.stats;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "stats.user", description = "Статистика пользователя")
public class UserStatisticsCommand implements CliCommand {
    private final UserService users;
    private final StatisticsService statistics;

    public UserStatisticsCommand(UserService users, StatisticsService statistics) {
        this.users = users;
        this.statistics = statistics;
    }

    @Override
    public void execute(ActionContext ctx) {
        var user = ctx.choose("Пользователи", users.findAll(), EntityView::user, User::getId);
        var stats = statistics.forUser(user.getId());
        ctx.output().println("Всего попыток: " + stats.total());
        ctx.output().println("Начатых попыток: " + stats.inProgress());
        ctx.output().println("Ожидают проверки: " + stats.submitted());
        ctx.output().println("Правильных ответов: " + stats.correct());
        ctx.output().println("Неправильных ответов: " + stats.incorrect());
        ctx.output().printf("Процент правильных среди оценённых: %.2f%%%n", stats.correctPercent());
        ctx.output().printf("Средняя оценка: %.2f%n", stats.averageScore());
    }
}
