package be.darkkraft.transferproxy.network.packet.config.clientbound;

import be.darkkraft.transferproxy.api.network.packet.Packet;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static be.darkkraft.transferproxy.util.BufUtil.*;

public record TransferPacket(String host, int port) implements Packet {

    public TransferPacket(final @NotNull ByteBuf buf) {
        this(readString(buf), readVarInt(buf));
    }

    @Override
    public void write(final @NotNull ByteBuf buf) {
        writeString(buf, this.host);
        writeVarInt(buf, this.port);
    }

    @Override
    public int getId() {
        return 0x0A;
    }

}