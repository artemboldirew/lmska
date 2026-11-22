package github.artemboldirew.lmska.cli.commands.user;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "user.create", description = "Создание пользователя")
public class CreateUser implements CliCommand {
    private final UserService users;

    public CreateUser(UserService users) {
        this.users = users;
    }

    @Override
    public void execute(ActionContext ctx) {
        String username = ctx.read("Имя пользователя: ");
        String email = ctx.read("Email: ");
        var value = users.create(username, email);
        ctx.output().println("Запись создана. " + EntityView.user(value));
    }
}
