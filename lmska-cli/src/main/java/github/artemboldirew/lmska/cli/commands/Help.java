package github.artemboldirew.lmska.cli.commands;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.*;
import github.artemboldirew.lmska.core.entity.*;
import github.artemboldirew.lmska.core.service.*;

@Component
@Command(name = "help", description = "Список команд")
public class Help implements CliCommand {
    private final CommandRegistry registry;

    public Help(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void execute(ActionContext ctx) {
        registry.commands().forEach((name, command) -> {
            Command metadata = command.getClass().getAnnotation(Command.class);
            ctx.output().println(name + " — " + metadata.description());
        });
        ctx.output().println("При заполнении команды: back — отмена; Enter при редактировании — сохранить значение.");
    }
}
