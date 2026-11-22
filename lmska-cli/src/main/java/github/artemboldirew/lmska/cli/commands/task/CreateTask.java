package github.artemboldirew.lmska.cli.commands.task;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "task.create", description = "Создание задания")
public class CreateTask implements CliCommand {
    private final TaskService tasks;
    private final CategoryService categories;

    public CreateTask(TaskService tasks, CategoryService categories) {
        this.tasks = tasks;
        this.categories = categories;
    }

    @Override
    public void execute(ActionContext ctx) {
        var category = ctx.choose("Доступные категории", categories.findAll(), EntityView::category, Category::getId);
        String title = ctx.read("Название задания: ");
        String description = ctx.read("Описание задания: ");
        var value = tasks.create(category.getId(), title, description);
        ctx.output().println("Запись создана. " + EntityView.task(value));
    }
}
