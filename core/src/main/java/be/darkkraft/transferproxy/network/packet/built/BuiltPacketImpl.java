package be.darkkraft.transferproxy.network.packet.built;

import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.api.network.packet.built.BuiltPacket;
import be.darkkraft.transferproxy.util.BufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

public class BuiltPacketImpl implements BuiltPacket {

    private final byte[] data;

    public BuiltPacketImpl(final @NotNull Packet packet) {
        final ByteBuf buf = Unpooled.buffer();
        BufUtil.writeVarInt(buf, packet.getId());
        packet.write(buf);
        buf.capacity(buf.readableBytes());

        this.data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), this.data);
    }

    public BuiltPacketImpl(final byte[] data) {
        this.data = data;
    }

    @Override
    public ByteBuf get(final @NotNull ByteBufAllocator allocator) {
        final int length = this.data.length;
        final ByteBuf buf = allocator.buffer(length, length);
        buf.writeBytes(this.data);
        return buf;
    }

}