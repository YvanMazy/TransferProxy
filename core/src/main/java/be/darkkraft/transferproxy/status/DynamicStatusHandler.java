package be.darkkraft.transferproxy.status;

import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.status.StatusHandler;
import be.darkkraft.transferproxy.api.status.response.StatusResponse;
import be.darkkraft.transferproxy.network.packet.status.clientbound.StatusResponsePacket;
import org.jetbrains.annotations.NotNull;

public abstract class DynamicStatusHandler implements StatusHandler {

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        connection.sendPacket(this.buildPacket(connection));
    }

    protected StatusResponsePacket buildPacket(final @NotNull PlayerConnection connection) {
        return new StatusResponsePacket(this.buildResponse(connection));
    }

    protected abstract StatusResponse buildResponse(final @NotNull PlayerConnection connection);

}