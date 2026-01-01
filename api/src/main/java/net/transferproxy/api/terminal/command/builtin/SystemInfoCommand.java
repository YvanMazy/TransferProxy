/*
 * MIT License
 *
 * Copyright (c) 2026 Yvan Mazy
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

package net.transferproxy.api.terminal.command.builtin;

import net.transferproxy.api.terminal.command.TerminalCommandExecutor;
import net.transferproxy.api.util.FormatUtil;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.Instant;

import static org.fusesource.jansi.Ansi.ansi;

public final class SystemInfoCommand implements TerminalCommandExecutor {

    @Override
    public void execute(final String @NotNull [] args) {
        final Ansi ansi = ansi().fgBrightGreen().a("JVM Information:");

        // JVM Information
        ansi.newline().fgBrightBlack().a("Java Version: ").fgDefault().a(System.getProperty("java.version"));
        ansi.newline().fgBrightBlack().a("JVM Name: ").fgDefault().a(System.getProperty("java.vm.name"));
        ansi.newline().fgBrightBlack().a("Java Home: ").fgDefault().a(System.getProperty("java.home"));

        // System Information
        final Runtime runtime = Runtime.getRuntime();
        ansi.newline().newline().fgBrightGreen().a("System Information:");
        ansi.newline()
                .fgBrightBlack()
                .a("OS: ")
                .fgDefault()
                .a(System.getProperty("os.name") + " ")
                .fgBrightBlack()
                .a(System.getProperty("os.version"));
        ansi.newline().fgBrightBlack().a("Architecture: ").fgDefault().a(System.getProperty("os.arch"));
        ansi.newline().fgBrightBlack().a("Available Processors: ").fgDefault().a(runtime.availableProcessors());

        // Memory Information
        final long maxMemory = runtime.maxMemory();
        final long totalMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        final long usedMemory = totalMemory - freeMemory;

        ansi.newline().newline().fgBrightGreen().a("Memory Usage:");
        ansi.newline().fgBrightBlack().a("Max Memory: ").fgDefault().a(FormatUtil.formatSize(maxMemory));
        ansi.newline().fgBrightBlack().a("Total Memory: ").fgDefault().a(FormatUtil.formatSize(totalMemory));
        ansi.newline().fgBrightBlack().a("Used Memory: ").fgDefault().a(FormatUtil.formatSize(usedMemory));
        ansi.newline().fgBrightBlack().a("Free Memory: ").fgDefault().a(FormatUtil.formatSize(freeMemory));

        // Heap and Non-Heap Memory
        final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        final long heapMemory = memoryBean.getHeapMemoryUsage().getUsed();
        final long nonHeapMemory = memoryBean.getNonHeapMemoryUsage().getUsed();

        ansi.newline().fgBrightBlack().a("Heap Memory Usage: ").fgDefault().a(FormatUtil.formatSize(heapMemory));
        ansi.newline().fgBrightBlack().a("Non-Heap Memory Usage: ").fgDefault().a(FormatUtil.formatSize(nonHeapMemory));

        // Server Information
        final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        final long uptime = runtimeBean.getUptime();
        final Duration uptimeDuration = Duration.ofMillis(uptime);

        ansi.newline().newline().fgBrightGreen().a("Server Information:");
        ansi.newline().fgBrightBlack().a("Uptime: ").fgDefault().a(FormatUtil.formatDuration(uptimeDuration));
        ansi.newline().fgBrightBlack().a("Start Time: ").fgDefault().a(Instant.ofEpochMilli(runtimeBean.getStartTime()).toString());

        System.out.println(ansi.reset());
    }

}