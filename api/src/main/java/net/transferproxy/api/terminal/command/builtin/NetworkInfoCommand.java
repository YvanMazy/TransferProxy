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