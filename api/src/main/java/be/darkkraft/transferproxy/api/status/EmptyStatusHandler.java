package be.darkkraft.transferproxy.api.status;

import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import org.jetbrains.annotations.NotNull;

public final class EmptyStatusHandler implements StatusHandler {

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        connection.forceDisconnect();
    }

}