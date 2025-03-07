/*
 * MIT License
 *
 * Copyright (c) 2025 Yvan Mazy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.transferproxy.api.terminal;

import net.transferproxy.api.terminal.command.TerminalCommandExecutor;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.fusesource.jansi.Ansi.ansi;

public abstract class BaseTerminalExecutor implements TerminalExecutor {

    private static final String[] EMPTY_ARGS = new String[0];

    private final Map<String, CommandEntry> commandMap = new HashMap<>();

    protected BaseTerminalExecutor() {
        this.register("help", "Display available commands.", args -> this.displayHelp());
    }

    @Override
    public void execute(final @NotNull String input) {
        if (input.isBlank() || (input.length() == 1 && !Character.isLetterOrDigit(input.charAt(0)))) {
            return;
        }
        final String[] args = input.split(" ");
        final CommandEntry entry = this.commandMap.get(args[0]);
        if (entry == null) {
            System.out.println("Unknown command '" + args[0] + "'. Type 'help' for available commands.");
            return;
        }
        final String[] newArgs;
        if (args.length > 1) {
            newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        } else {
            newArgs = EMPTY_ARGS;
        }
        entry.executor().execute(newArgs);
    }

    public void register(final @NotNull String name, final @NotNull String description, final @NotNull TerminalCommandExecutor executor) {
        this.register(new CommandEntry(name, description, executor));
    }

    public void register(final @NotNull CommandEntry entry) {
        this.commandMap.put(entry.name(), entry);
    }

    protected void displayHelp() {
        final Ansi ansi = ansi().fgBrightGreen().a("Available commands:");
        for (final CommandEntry entry : this.commandMap.values()) {
            ansi.newline().fgBrightBlack().a(entry.name()).append(": ").fgDefault().a(entry.description());
        }
        System.out.println(ansi.reset());
    }

    public record CommandEntry(@NotNull String name, @NotNull String description, @NotNull TerminalCommandExecutor executor) {

        public CommandEntry {
            Objects.requireNonNull(name, "name must not be null");
            if (name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            Objects.requireNonNull(description, "description must not be null");
            Objects.requireNonNull(executor, "executor must not be null");
        }

    }

}