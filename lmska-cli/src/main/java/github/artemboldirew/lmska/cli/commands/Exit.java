package github.artemboldirew.lmska.cli.commands;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "exit", description = "Завершение приложения")
public class Exit implements CliCommand {

    @Override
    public void execute(ActionContext ctx) {
        ctx.stop();
    }
}
