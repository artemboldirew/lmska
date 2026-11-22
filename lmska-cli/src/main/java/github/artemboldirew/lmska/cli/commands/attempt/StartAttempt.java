package github.artemboldirew.lmska.cli.commands.attempt;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "attempt.start", description = "Начало выполнения задания")
public class StartAttempt implements CliCommand {
    private final TaskAttemptService attempts;
    private final UserService users;
    private final TaskService tasks;

    public StartAttempt(TaskAttemptService attempts, UserService users, TaskService tasks) {
        this.attempts = attempts;
        this.users = users;
        this.tasks = tasks;
    }

    @Override
    public void execute(ActionContext ctx) {
        var user = ctx.choose("Пользователи", users.findAll(), EntityView::user, User::getId);
        var task = ctx.choose("Задания", tasks.findAll(), EntityView::task, Task::getId);
        var attempt = attempts.create(user.getId(), task.getId());
        ctx.output().println("Попытка создана. " + EntityView.attempt(attempt));
    }
}
