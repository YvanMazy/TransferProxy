package be.darkkraft.transferproxy.api.login;

import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;

public final class EmptyLoginHandler implements LoginHandler {

    @Override
    public void handle(final PlayerConnection connection) {
        connection.forceDisconnect();
    }

}