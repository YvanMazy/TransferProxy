package be.darkkraft.transferproxy.api.login;

import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;

/**
 * The handler used to handle every login request
 */
@FunctionalInterface
public interface LoginHandler {

    /**
     * Handle the login request
     *
     * @param connection The {@link PlayerConnection} who request
     */
    void handle(final PlayerConnection connection);

}