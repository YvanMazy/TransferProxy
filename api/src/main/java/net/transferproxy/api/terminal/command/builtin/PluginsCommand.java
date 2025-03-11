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

package net.transferproxy.api.terminal.command.builtin;

import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.plugin.Plugin;
import net.transferproxy.api.plugin.info.PluginInfo;
import net.transferproxy.api.terminal.command.TerminalCommandExecutor;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static org.fusesource.jansi.Ansi.ansi;

public final class PluginsCommand implements TerminalCommandExecutor {

    @Override
    public void execute(final String @NotNull [] args) {
        final Collection<Plugin> plugins = TransferProxy.getInstance().getModuleManager().getPluginManager().getPlugins();

        if (plugins.isEmpty()) {
            System.out.println(ansi().fgBrightRed().a("No plugins loaded.").reset());
            return;
        }

        final Ansi ansi = ansi().fgBrightGreen().a("Loaded Plugins (" + plugins.size() + "):");

        for (final Plugin plugin : plugins) {
            final PluginInfo info = plugin.getInfo();
            ansi.newline()
                    .fgBrightBlack()
                    .a("- ")
                    .fgBrightCyan()
                    .a(info.getName())
                    .fgBrightBlack()
                    .a(" v")
                    .fgDefault()
                    .a(info.getVersion())
                    .fgBrightBlack()
                    .a(" by ")
                    .fgDefault()
                    .a(info.getAuthor());

            if (!info.getDescription().isEmpty()) {
                ansi.newline().fgBrightBlack().a("  ").a(info.getDescription());
            }
        }

        System.out.println(ansi.reset());
    }

}