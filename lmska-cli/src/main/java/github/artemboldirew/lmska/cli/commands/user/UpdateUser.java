package github.artemboldirew.lmska.cli.commands.user;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "user.update", description = "Изменение пользователя")
public class UpdateUser implements CliCommand {
    private final UserService users;

    public UpdateUser(UserService users) {
        this.users = users;
    }

    @Override
    public void execute(ActionContext ctx) {
        var value = ctx.choose("Пользователи", users.findAll(), EntityView::user, User::getId);
        String username = ctx.edit("Имя пользователя", value.getUsername());
        String email = ctx.edit("Email", value.getEmail());
        users.update(value.getId(), username, email);
        ctx.output().println("Запись обновлена.");
    }
}
