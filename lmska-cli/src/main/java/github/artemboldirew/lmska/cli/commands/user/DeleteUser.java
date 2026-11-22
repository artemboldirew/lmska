package github.artemboldirew.lmska.cli.commands.user;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "user.delete", description = "Удаление пользователя")
public class DeleteUser implements CliCommand {
    private final UserService users;

    public DeleteUser(UserService users) {
        this.users = users;
    }

    @Override
    public void execute(ActionContext ctx) {
        var value = ctx.choose("Пользователи", users.findAll(), EntityView::user, User::getId);
        if (ctx.confirm("Удалить " + EntityView.user(value) + "?")) {
            users.delete(value.getId());
            ctx.output().println("Запись удалена.");
        } else ctx.output().println("Удаление отменено.");
    }
}
