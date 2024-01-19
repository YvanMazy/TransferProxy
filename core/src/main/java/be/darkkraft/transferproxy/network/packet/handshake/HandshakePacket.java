package be.darkkraft.transferproxy.network.packet.handshake;

import be.darkkraft.transferproxy.api.network.connection.ConnectionState;
import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static be.darkkraft.transferproxy.util.BufUtil.*;

public record HandshakePacket(int protocol, String hostname, int hostPort, ConnectionState nextState) implements ServerboundPacket {

    public HandshakePacket(final @NotNull ByteBuf buf) {
        this(readVarInt(buf), readString(buf), buf.readShort(), ConnectionState.fromId(readVarInt(buf)));
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        connection.setProtocol(this.protocol);
        connection.setHost(this.hostname, this.hostPort);
        connection.setState(this.nextState);
    }

    @Override
    public void write(final @NotNull ByteBuf buf) {
        writeVarInt(buf, this.protocol);
        writeString(buf, this.hostname);
        buf.writeShort(this.hostPort);
        writeVarInt(buf, this.nextState.ordinal());
    }

    @Override
    public int getId() {
        return 0x00;
    }

}