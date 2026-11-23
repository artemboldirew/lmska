package github.artemboldirew.lmska.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Function;

/**
 * Читает ввод и выводит сообщения консоли
 */
public final class ActionContext implements AutoCloseable {
    private final InputStream input;
    private final PrintStream output;
    private final PrintStream error;
    private final Scanner scanner;
    private boolean running = true;

    public ActionContext(InputStream input, PrintStream output, PrintStream error) {
        this.input = input;
        this.output = output;
        this.error = error;
        this.scanner = new Scanner(input, System.console() == null ? Charset.defaultCharset() : System.console().charset());
    }

    public InputStream input() {
        return input;
    }
    public PrintStream output() {
        return output;
    }
    public PrintStream error() {
        return error;
    }
    public boolean running() {
        return running;
    }
    public void stop() {
        running = false;
    }

    public String nextCommand() {
        output.print("lmska> ");
        output.flush();
        if (!scanner.hasNextLine()) {
            stop();
            return null;
        }
        return scanner.nextLine().trim();
    }

    /**
     * Читает значение
     * back отменяет команду, конец ввода завершает приложение
     */
    public String read(String prompt) {
        output.print(prompt);
        output.flush();
        if (!scanner.hasNextLine()) {
            stop();
            throw new Cancelled();
        }
        String value = scanner.nextLine().trim();
        if (value.equalsIgnoreCase("back")) throw new Cancelled();
        return value;
    }

    public String edit(String label, String current) {
        String value = read(label + " [" + (current == null ? "" : current) + "] (Enter — сохранить): ");
        return value.isEmpty() ? current : value;
    }

    /**
     * Показывает список и предлагает выбрать запись по номеру или UUID
     */
    public <T> T choose(String label, List<T> items, Function<T, String> describe, Function<T, UUID> id) {
        if (items.isEmpty()) throw new IllegalArgumentException("Нет доступных записей: " + label);
        output.println(label + ":");
        for (int i = 0; i < items.size(); i++) output.println((i + 1) + ". " + describe.apply(items.get(i)));
        while (true) {
            String value = read("Номер или UUID (back — отмена): ");
            try {
                if (value.matches("[0-9]+")) {
                    int index = Integer.parseInt(value) - 1;
                    if (index >= 0 && index < items.size()) return items.get(index);
                } else if (value.matches("[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}")) {
                    UUID key = UUID.fromString(value);
                    for (T item : items) if (id.apply(item).equals(key)) return item;
                }
            } catch (IllegalArgumentException ignored) {
            }
            error.println("Неверный номер или UUID. Выберите существующую запись.");
        }
    }

    /**
     * Запрашивает подтверждение; принимаются да/нет и yes/no
     */
    public boolean confirm(String prompt) {
        while (true) {
            String value = read(prompt + " (да/нет): ");
            if (value.equalsIgnoreCase("да") || value.equalsIgnoreCase("yes")) return true;
            if (value.isEmpty() || value.equalsIgnoreCase("нет") || value.equalsIgnoreCase("no")) return false;
            error.println("Введите да или нет.");
        }
    }

    /**
     * Читает число, повторяя запрос при неверном формате
     */
    public double number(String prompt) {
        while (true) {
            try {
                double value = Double.parseDouble(read(prompt).replace(',', '.'));
                if (Double.isFinite(value)) return value;
            } catch (NumberFormatException ignored) {
            }
            error.println("Введите конечное число.");
        }
    }

    @Override
    public void close() {
        scanner.close();
    }

    /**
     * Отменяет текущую команду
     */
    public static final class Cancelled extends RuntimeException {
    }
}
