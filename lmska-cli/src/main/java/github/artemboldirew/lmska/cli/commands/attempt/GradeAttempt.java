package github.artemboldirew.lmska.cli.commands.attempt;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "attempt.grade", description = "Ручная оценка отправленного ответа")
public class GradeAttempt implements CliCommand {
    private final TaskAttemptService attempts;

    public GradeAttempt(TaskAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void execute(ActionContext ctx) {
        var attempt = ctx.choose("Попытки", attempts.findAll(), EntityView::attempt, TaskAttempt::getId);
        ctx.output().println(EntityView.attempt(attempt));
        double score = ctx.number("Оценка (неотрицательное число): ");
        boolean correct = ctx.confirm("Ответ правильный?");
        attempts.grade(attempt.getId(), score, correct);
        ctx.output().println("Оценка сохранена.");
    }
}
