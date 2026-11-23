package github.artemboldirew.lmska.cli.commands.user;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "user.list", description = "Список пользователей")
public class ListUser implements CliCommand {
    private final UserService users;

    public ListUser(UserService users) {
        this.users = users;
    }

    @Override
    public void execute(ActionContext ctx) {
        var values = users.findAll();
        if (values.isEmpty()) ctx.output().println("Список пуст.");
        values.forEach(value -> ctx.output().println(EntityView.user(value)));
    }
}
