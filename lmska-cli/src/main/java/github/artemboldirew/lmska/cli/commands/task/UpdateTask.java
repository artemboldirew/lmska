package github.artemboldirew.lmska.cli.commands.task;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "task.update", description = "Изменение задания")
public class UpdateTask implements CliCommand {
    private final TaskService tasks;
    private final CategoryService categories;

    public UpdateTask(TaskService tasks, CategoryService categories) {
        this.tasks = tasks;
        this.categories = categories;
    }

    @Override
    public void execute(ActionContext ctx) {
        var value = ctx.choose("Задания", tasks.findAll(), EntityView::task, Task::getId);
        var category = value.getCategory();
        if (ctx.confirm("Текущая категория: " + category.getName() + ". Изменить?")) {
            category = ctx.choose("Категории", categories.findAll(), EntityView::category, Category::getId);
        }
        String title = ctx.edit("Название задания", value.getTitle());
        String description = ctx.edit("Описание задания", value.getDescription());
        tasks.update(value.getId(), category.getId(), title, description);
        ctx.output().println("Запись обновлена.");
    }
}
