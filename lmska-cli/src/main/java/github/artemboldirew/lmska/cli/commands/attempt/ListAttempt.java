package github.artemboldirew.lmska.cli.commands.attempt;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "attempt.list", description = "Список попыток")
public class ListAttempt implements CliCommand {
    private final TaskAttemptService attempts;

    public ListAttempt(TaskAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void execute(ActionContext ctx) {
        var values = attempts.findAll();
        if (values.isEmpty()) ctx.output().println("Список пуст.");
        values.forEach(value -> ctx.output().println(EntityView.attempt(value)));
    }
}
