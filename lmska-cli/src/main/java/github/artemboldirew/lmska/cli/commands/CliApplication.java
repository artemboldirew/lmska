package github.artemboldirew.lmska.cli.commands;

import com.github.artemboldirew.lmska.bring.ApplicationRunner;
import com.github.artemboldirew.lmska.bring.Environment;
import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.cli.ActionContext;
import github.artemboldirew.lmska.cli.CommandRegistry;

/**
 * Запускает консоль, если включена настройка use-cli
 */
@Component
public class CliApplication implements ApplicationRunner {
    private final CommandRegistry registry;
    private final Environment environment;

    public CliApplication(CommandRegistry registry, Environment environment) {
        this.registry = registry;
        this.environment = environment;
    }

    /**
     * Читает и выполняет команды до exit или конца ввода
     */
    @Override
    public void run() {
        if (!Boolean.parseBoolean(environment.getProperty("use-cli", "false"))) return;
        registry.registry();
        try (ActionContext ctx = new ActionContext(System.in, System.out, System.err)) {
            ctx.output().println("LMSKA\nВведите help для просмотра команд. back — отмена заполнения.\n");
            while (ctx.running()) {
                String name = ctx.nextCommand();
                if (name == null) break;
                if (name.isBlank()) continue;
                var command = registry.find(name);
                if (command == null) {
                    ctx.error().println("Неизвестная команда. Введите help для просмотра команд.");
                    continue;
                }
                try {
                    command.execute(ctx);
                } catch (ActionContext.Cancelled e) {
                    if (ctx.running()) ctx.output().println("Команда отменена.");
                } catch (IllegalArgumentException | IllegalStateException e) {
                    ctx.error().println("Ошибка: " + e.getMessage());
                }
            }
            ctx.output().println("До свидания!");
        }
    }
}
