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

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.network.connection.ConnectionState;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.terminal.command.TerminalCommandExecutor;
import net.transferproxy.api.util.FormatUtil;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Objects;

import static org.fusesource.jansi.Ansi.ansi;

public final class NetworkInfoCommand implements TerminalCommandExecutor {

    private static final int DEFAULT_DISPLAYED_ITEMS = 10;

    @Override
    public void execute(final String @NotNull [] args) {
        final Ansi ansi = ansi().fgBrightGreen().a("Network Information:");

        // Uptime information
        final long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        final Duration uptimeDuration = Duration.ofMillis(uptime);
        ansi.newline().fgBrightBlack().a("Uptime: ").fgDefault().a(FormatUtil.formatDuration(uptimeDuration));

        // Connection information
        final ChannelGroup channelGroup = TransferProxy.getInstance().getNetworkServer().getGroup();
        final int connectionCount = channelGroup.size();
        ansi.newline().fgBrightBlack().a("Active Connections: ").fgDefault().a(connectionCount);

        // List actives connections
        if (connectionCount > 0) {
            int displayedItems = DEFAULT_DISPLAYED_ITEMS;
            if (args.length > 0) {
                try {
                    displayedItems = Math.max(Integer.parseInt(args[0]), 0);
                } catch (final NumberFormatException ignored) {
                }
            }

            ansi.newline().fgBrightBlack().a("Connected Channels:");

            int i = 0;
            for (final Channel channel : channelGroup) {
                if (++i > displayedItems) {
                    // Add "and X more..."
                    final int remaining = connectionCount - displayedItems;
                    ansi.newline().fgBrightYellow().a("and ").a(remaining).a(" more...");
                    break;
                }

                ansi.newline().fgBrightBlack().a(" - Channel ID: ").fgDefault().a(channel.id().asShortText());

                final String stateName;
                if (channel.pipeline().get("handler") instanceof final PlayerConnection connection) {
                    final ConnectionState state = connection.getState();
                    stateName = state.name();

                    if (state.isLogin()) {
                        ansi.fgBrightBlack().a(" | Name: ").fgDefault().a(Objects.requireNonNullElse(connection.getName(), "-"));
                        ansi.fgBrightBlack().a(" | Protocol: ").fgDefault().a(connection.getProtocol());
                    }
                } else {
                    stateName = "INIT";
                }

                ansi.fgBrightBlack().a(" | State: ").fgDefault().a(stateName);
                ansi.fgBrightBlack().a(" | Address: ").fgDefault().a(channel.remoteAddress());
            }
        }

        System.out.println(ansi.reset());
    }

}