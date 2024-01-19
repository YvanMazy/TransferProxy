package be.darkkraft.transferproxy.api.status;

import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import org.jetbrains.annotations.NotNull;

/**
 * The handler used to handle every status request
 */
@FunctionalInterface
public interface StatusHandler {

    /**
     * Handle the status request
     *
     * @param connection The {@link PlayerConnection} who request
     */
    void handle(final @NotNull PlayerConnection connection);

}