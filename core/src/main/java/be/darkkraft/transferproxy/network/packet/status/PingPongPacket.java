package be.darkkraft.transferproxy.network.packet.status;

import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.built.BuiltPacket;
import be.darkkraft.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import be.darkkraft.transferproxy.network.packet.built.BuiltPacketImpl;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public record PingPongPacket(long payload) implements ServerboundPacket {

    private static final BuiltPacket PONG_PACKET = new BuiltPacketImpl(new PingPongPacket(0L));

    public PingPongPacket(final @NotNull ByteBuf buf) {
        this(buf.readLong());
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        connection.sendPacketAndClose(PONG_PACKET);
    }

    @Override
    public void write(final @NotNull ByteBuf buf) {
        buf.writeLong(this.payload);
    }

    @Override
    public int getId() {
        return 0x01;
    }

}