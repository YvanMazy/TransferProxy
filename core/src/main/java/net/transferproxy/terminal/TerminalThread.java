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

package net.transferproxy.terminal;

import net.transferproxy.api.terminal.TerminalExecutor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class TerminalThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminalThread.class);

    private final BooleanSupplier runningSupplier;
    private final Supplier<TerminalExecutor> executor;

    private BufferedReader reader;

    public TerminalThread(final @NotNull BooleanSupplier runningSupplier, final @NotNull Supplier<TerminalExecutor> executorSupplier) {
        super("Terminal Thread");
        this.runningSupplier = Objects.requireNonNull(runningSupplier, "runningSupplier must not be null");
        this.executor = Objects.requireNonNull(executorSupplier, "executorSupplier must not be null");
    }

    @Override
    public void run() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));

        boolean lastExecuted = true;
        String input;
        while (this.runningSupplier.getAsBoolean()) {
            try {
                if (lastExecuted) {
                    System.out.print("> ");
                    lastExecuted = false;
                }
                final String line = this.reader.readLine();
                if (line == null) {
                    continue;
                }

                input = line.trim();
                if (input.isBlank()) {
                    continue;
                }
            } catch (final IllegalStateException ignored) {
                break;
            } catch (final NoSuchElementException ignored) {
                continue;
            } catch (final Exception e) {
                LOGGER.error("Failed to read input", e);
                continue;
            }
            try {
                lastExecuted = true;
                this.executor.get().execute(input);
            } catch (final Throwable e) {
                LOGGER.error("Failed to execute command", e);
            }
        }
        this.close();
    }

    public void close() {
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (final IOException e) {
                LOGGER.error("Failed to close terminal reader", e);
            }
        }
    }

}