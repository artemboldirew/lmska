package github.artemboldirew.lmska.cli.commands.attempt;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "attempt.get", description = "Просмотр попытки")
public class GetAttempt implements CliCommand {
    private final TaskAttemptService attempts;

    public GetAttempt(TaskAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void execute(ActionContext ctx) {
        var attempt = ctx.choose("Попытки", attempts.findAll(), EntityView::attempt, TaskAttempt::getId);
        ctx.output().println(EntityView.attempt(attempt));
    }
}
