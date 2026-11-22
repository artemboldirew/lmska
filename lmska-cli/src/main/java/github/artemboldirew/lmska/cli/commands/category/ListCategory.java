package github.artemboldirew.lmska.cli.commands.category;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "category.list", description = "Список категорий")
public class ListCategory implements CliCommand {
    private final CategoryService categories;

    public ListCategory(CategoryService categories) {
        this.categories = categories;
    }

    @Override
    public void execute(ActionContext ctx) {
        var values = categories.findAll();
        if (values.isEmpty()) ctx.output().println("Список пуст.");
        values.forEach(value -> ctx.output().println(EntityView.category(value)));
    }
}
