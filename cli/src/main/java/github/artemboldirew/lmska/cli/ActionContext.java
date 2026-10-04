package github.artemboldirew.lmska.cli;

import java.io.InputStream;
import java.io.PrintStream;

public final class ActionContext {
    private final InputStream  input;
    private final PrintStream output;
    private final PrintStream error;

    public ActionContext(
            InputStream input,
            PrintStream output,
            PrintStream error
    ) {
        this.input = input;
        this.output = output;
        this.error = error;
    }

    public InputStream  input() {
        return input;
    }

    public PrintStream output() {
        return output;
    }

    public PrintStream error() {
        return error;
    }
}