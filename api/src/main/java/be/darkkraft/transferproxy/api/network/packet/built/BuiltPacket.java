package be.darkkraft.transferproxy.api.network.packet.built;

import be.darkkraft.transferproxy.api.network.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.jetbrains.annotations.NotNull;

public interface BuiltPacket extends Packet {

    ByteBuf get(final @NotNull ByteBufAllocator allocator);

    @Override
    default void write(final @NotNull ByteBuf buf) {
        throwIllegalState();
    }

    @Override
    default int getId() {
        throwIllegalState();
        return 0x00;
    }

    private static void throwIllegalState() {
        throw new IllegalStateException("A snapshot packet must be sent with the #get() method");
    }

}