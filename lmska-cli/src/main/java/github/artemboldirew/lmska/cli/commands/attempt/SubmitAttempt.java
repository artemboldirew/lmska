package github.artemboldirew.lmska.cli.commands.attempt;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "attempt.submit", description = "Отправка ответа на проверку")
public class SubmitAttempt implements CliCommand {
    private final TaskAttemptService attempts;

    public SubmitAttempt(TaskAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void execute(ActionContext ctx) {
        var attempt = ctx.choose("Попытки", attempts.findAll(), EntityView::attempt, TaskAttempt::getId);
        String answer = ctx.edit("Ответ", attempt.getAnswer());
        attempts.submit(attempt.getId(), answer);
        ctx.output().println("Ответ отправлен.");
    }
}
