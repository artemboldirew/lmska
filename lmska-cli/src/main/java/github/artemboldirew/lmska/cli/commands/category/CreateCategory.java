package github.artemboldirew.lmska.cli.commands.category;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "category.create", description = "Создание категории")
public class CreateCategory implements CliCommand {
    private final CategoryService categories;

    public CreateCategory(CategoryService categories) {
        this.categories = categories;
    }

    @Override
    public void execute(ActionContext ctx) {
        String name = ctx.read("Название категории: ");
        String description = ctx.read("Описание: ");
        var value = categories.create(name, description);
        ctx.output().println("Запись создана. " + EntityView.category(value));
    }
}
