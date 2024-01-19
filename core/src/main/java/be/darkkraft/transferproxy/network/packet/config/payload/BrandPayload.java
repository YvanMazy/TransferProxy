package be.darkkraft.transferproxy.network.packet.config.payload;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static be.darkkraft.transferproxy.util.BufUtil.readString;
import static be.darkkraft.transferproxy.util.BufUtil.writeString;

public record BrandPayload(String brand) implements PayloadData {

    public BrandPayload(final @NotNull ByteBuf buf) {
        this(readString(buf));
    }

    @Override
    public void write(final @NotNull ByteBuf buf) {
        writeString(buf, this.brand);
    }

    @Override
    public String getChannel() {
        return "minecraft:brand";
    }

}