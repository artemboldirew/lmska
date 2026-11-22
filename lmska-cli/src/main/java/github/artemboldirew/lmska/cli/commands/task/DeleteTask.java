package github.artemboldirew.lmska.cli.commands.task;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "task.delete", description = "Удаление задания")
public class DeleteTask implements CliCommand {
    private final TaskService tasks;

    public DeleteTask(TaskService tasks) {
        this.tasks = tasks;
    }

    @Override
    public void execute(ActionContext ctx) {
        var value = ctx.choose("Задания", tasks.findAll(), EntityView::task, Task::getId);
        if (ctx.confirm("Удалить " + EntityView.task(value) + "?")) {
            tasks.delete(value.getId());
            ctx.output().println("Запись удалена.");
        } else ctx.output().println("Удаление отменено.");
    }
}
