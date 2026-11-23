package github.artemboldirew.lmska.cli.commands.category;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "category.get", description = "Просмотр категории")
public class GetCategory implements CliCommand {
    private final CategoryService categories;

    public GetCategory(CategoryService categories) {
        this.categories = categories;
    }

    @Override
    public void execute(ActionContext ctx) {
        var value = ctx.choose("Категории", categories.findAll(), EntityView::category, Category::getId);
        ctx.output().println(EntityView.category(value));
    }
}
