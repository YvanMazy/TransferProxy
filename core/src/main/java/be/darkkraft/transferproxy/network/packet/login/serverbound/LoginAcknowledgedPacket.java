package be.darkkraft.transferproxy.network.packet.login.serverbound;

import be.darkkraft.transferproxy.api.network.connection.ConnectionState;
import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public record LoginAcknowledgedPacket() implements ServerboundPacket {

    public LoginAcknowledgedPacket(final @SuppressWarnings("unused") @NotNull ByteBuf buf) {
        this();
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        if (connection.getName() == null) {
            connection.forceDisconnect();
            return;
        }
        connection.setState(ConnectionState.CONFIG);
    }

    @Override
    public void write(final @NotNull ByteBuf buf) {
    }

    @Override
    public int getId() {
        return 0x03;
    }

}