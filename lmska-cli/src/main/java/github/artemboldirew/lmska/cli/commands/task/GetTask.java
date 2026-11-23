package github.artemboldirew.lmska.cli.commands.task;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "task.get", description = "Просмотр задания")
public class GetTask implements CliCommand {
    private final TaskService tasks;

    public GetTask(TaskService tasks) {
        this.tasks = tasks;
    }

    @Override
    public void execute(ActionContext ctx) {
        var value = ctx.choose("Задания", tasks.findAll(), EntityView::task, Task::getId);
        ctx.output().println(EntityView.task(value));
    }
}
