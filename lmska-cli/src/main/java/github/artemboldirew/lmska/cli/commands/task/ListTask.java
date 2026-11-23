package github.artemboldirew.lmska.cli.commands.task;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "task.list", description = "Список заданий")
public class ListTask implements CliCommand {
    private final TaskService tasks;
    private final CategoryService categories;

    public ListTask(TaskService tasks, CategoryService categories) {
        this.tasks = tasks;
        this.categories = categories;
    }

    @Override
    public void execute(ActionContext ctx) {
        var values = tasks.findAll();
        if (ctx.confirm("Показать задания выбранной категории?")) {
            var category = ctx.choose("Категории", categories.findAll(), EntityView::category, Category::getId);
            values = tasks.findByCategory(category.getId());
        }
        if (values.isEmpty()) ctx.output().println("Список пуст.");
        values.forEach(value -> ctx.output().println(EntityView.task(value)));
    }
}
