package github.artemboldirew.lmska.cli.commands.attempt;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "attempt.delete", description = "Удаление попытки")
public class DeleteAttempt implements CliCommand {
    private final TaskAttemptService attempts;

    public DeleteAttempt(TaskAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void execute(ActionContext ctx) {
        var attempt = ctx.choose("Попытки", attempts.findAll(), EntityView::attempt, TaskAttempt::getId);
        if (ctx.confirm("Удалить попытку " + attempt.getId() + "?")) {
            attempts.delete(attempt.getId());
            ctx.output().println("Попытка удалена.");
        } else ctx.output().println("Удаление отменено.");
    }
}
