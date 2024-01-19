package be.darkkraft.transferproxy.status;

import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.status.StatusHandler;
import be.darkkraft.transferproxy.api.status.response.StatusResponse;
import be.darkkraft.transferproxy.network.packet.status.clientbound.StatusResponsePacket;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CachedStatusHandler implements StatusHandler {

    private final StatusResponse response;

    public CachedStatusHandler(final StatusResponse response) {
        this.response = Objects.requireNonNull(response, "Response cannot be null");
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        connection.sendPacket(new StatusResponsePacket(this.response));
    }

}