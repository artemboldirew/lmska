package github.artemboldirew.lmska.cli;

import com.github.artemboldirew.lmska.bring.ApplicationContext;
import com.github.artemboldirew.lmska.bring.annotation.Component;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Хранит команды по их названиям
 */
@Component
public class CommandRegistry {
    private final Map<String, CliCommand> commands = new TreeMap<>();
    private final ApplicationContext context;

    public CommandRegistry(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Собирает команды из готовых бинов с аннотацией Command
     */
    public void registry() {
        Map<String, CliCommand> found = new TreeMap<>();
        for (Object bean : context.getBeansWithAnnotation(Command.class)) {
            if (!(bean instanceof CliCommand command)) {
                throw new IllegalStateException("Бин с Command должен реализовывать CliCommand: " + bean.getClass().getName());
            }
            Command metadata = bean.getClass().getAnnotation(Command.class);
            if (metadata.name().isBlank()) {
                throw new IllegalArgumentException("Отсутствует название команды");
            }
            if (found.putIfAbsent(metadata.name(), command) != null) {
                throw new IllegalStateException("Повтор команды: " + metadata.name());
            }
        }
        commands.clear();
        commands.putAll(found);
    }

    public CliCommand find(String name) {
        return commands.get(name);
    }

    public Map<String, CliCommand> commands() {
        return Collections.unmodifiableMap(commands);
    }
}
