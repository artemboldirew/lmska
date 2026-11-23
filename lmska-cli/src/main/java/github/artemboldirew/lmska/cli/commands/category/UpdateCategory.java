package github.artemboldirew.lmska.cli.commands.category;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "category.update", description = "Изменение категории")
public class UpdateCategory implements CliCommand {
    private final CategoryService categories;

    public UpdateCategory(CategoryService categories) {
        this.categories = categories;
    }

    @Override
    public void execute(ActionContext ctx) {
        var value = ctx.choose("Категории", categories.findAll(), EntityView::category, Category::getId);
        String name = ctx.edit("Название категории", value.getName());
        String description = ctx.edit("Описание", value.getDescription());
        categories.update(value.getId(), name, description);
        ctx.output().println("Запись обновлена.");
    }
}
