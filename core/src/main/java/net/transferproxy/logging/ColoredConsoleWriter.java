/*
 * MIT License
 *
 * Copyright (c) 2024 Yvan Mazy
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

package net.transferproxy.logging;

import org.fusesource.jansi.AnsiConsole;
import org.tinylog.Level;
import org.tinylog.core.ConfigurationParser;
import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.provider.InternalLogger;
import org.tinylog.writers.AbstractFormatPatternWriter;

import java.util.Collection;
import java.util.Map;

public class ColoredConsoleWriter extends AbstractFormatPatternWriter {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[38;5;14m";

    private final Level errorLevel;
    private boolean ansi;

    public ColoredConsoleWriter() {
        this(Map.of());
    }

    public ColoredConsoleWriter(final Map<String, String> properties) {
        super(properties);
        Level levelStream = Level.WARN;
        String stream = this.getStringValue("stream");
        if (stream != null) {
            final String[] streams = stream.split("@", 2);
            if (streams.length == 2) {
                levelStream = ConfigurationParser.parse(streams[1], levelStream);
                if (!streams[0].equals("err")) {
                    InternalLogger.log(Level.ERROR, "Stream with level must be \"err\", \"" + streams[0] + "\" is an invalid name");
                }

                stream = null;
            }
        }

        if (stream == null) {
            this.errorLevel = levelStream;
        } else if ("err".equalsIgnoreCase(stream)) {
            this.errorLevel = Level.TRACE;
        } else if ("out".equalsIgnoreCase(stream)) {
            this.errorLevel = Level.OFF;
        } else {
            InternalLogger.log(Level.ERROR, "Stream must be \"out\" or \"err\", \"" + stream + "\" is an invalid stream name");
            this.errorLevel = levelStream;
        }

        this.ansi = !"true".equalsIgnoreCase(System.getenv("disable-ansi"));
        if (this.ansi) {
            AnsiConsole.systemInstall();
        }
    }

    @Override
    public Collection<LogEntryValue> getRequiredLogEntryValues() {
        final Collection<LogEntryValue> logEntryValues = super.getRequiredLogEntryValues();
        logEntryValues.add(LogEntryValue.LEVEL);
        return logEntryValues;
    }

    @Override
    public void write(final LogEntry logEntry) {
        final String render = this.render(logEntry);
        final Level level = logEntry.getLevel();

        (level.ordinal() < this.errorLevel.ordinal() ? System.out : System.err).print(this.ansi ? color(level, render) : render);
    }

    private static String color(final Level level, final String string) {
        if (level == Level.ERROR) {
            return ANSI_RED + string + ANSI_RESET;
        } else if (level == Level.WARN) {
            return ANSI_YELLOW + string + ANSI_RESET;
        } else if (level == Level.DEBUG) {
            return ANSI_BLUE + string + ANSI_RESET;
        }
        return string;
    }

    @Override
    public void flush() {
        // do nothing
    }

    @Override
    public void close() {
        // do nothing
    }

    public boolean isAnsi() {
        return this.ansi;
    }

    public void setAnsi(final boolean ansi) {
        this.ansi = ansi;
    }

}