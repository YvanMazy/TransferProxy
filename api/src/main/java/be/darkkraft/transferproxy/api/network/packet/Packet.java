package be.darkkraft.transferproxy.api.network.packet;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public interface Packet {

    void write(final @NotNull ByteBuf buf);

    int getId();

}