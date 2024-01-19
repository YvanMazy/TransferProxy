package be.darkkraft.transferproxy.network.packet.status.serverbound;

import be.darkkraft.transferproxy.api.TransferProxy;
import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public record StatusRequestPacket() implements ServerboundPacket {

    public StatusRequestPacket(final @SuppressWarnings("unused") @NotNull ByteBuf buf) {
        this();
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        TransferProxy.getInstance().getModuleManager().getStatusHandler().handle(connection);
    }

    @Override
    public void write(final @NotNull ByteBuf buf) {
    }

    @Override
    public int getId() {
        return 0x00;
    }

}