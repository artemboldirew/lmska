package github.artemboldirew.lmska.cli.commands.category;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "category.delete", description = "Удаление категории")
public class DeleteCategory implements CliCommand {
    private final CategoryService categories;

    public DeleteCategory(CategoryService categories) {
        this.categories = categories;
    }

    @Override
    public void execute(ActionContext ctx) {
        var value = ctx.choose("Категории", categories.findAll(), EntityView::category, Category::getId);
        if (ctx.confirm("Удалить " + EntityView.category(value) + "?")) {
            categories.delete(value.getId());
            ctx.output().println("Запись удалена.");
        } else ctx.output().println("Удаление отменено.");
    }
}
