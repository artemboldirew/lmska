package github.artemboldirew.lmska.cli.commands.user;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "user.get", description = "Просмотр пользователя")
public class GetUser implements CliCommand {
    private final UserService users;

    public GetUser(UserService users) {
        this.users = users;
    }

    @Override
    public void execute(ActionContext ctx) {
        var value = ctx.choose("Пользователи", users.findAll(), EntityView::user, User::getId);
        ctx.output().println(EntityView.user(value));
    }
}
