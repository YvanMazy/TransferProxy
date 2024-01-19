package be.darkkraft.transferproxy.network.packet.config.payload;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public interface PayloadData {

    void write(final @NotNull ByteBuf buf);

    String getChannel();

}