package be.darkkraft.transferproxy.network.packet.config;

import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import be.darkkraft.transferproxy.network.packet.config.payload.BrandPayload;
import be.darkkraft.transferproxy.network.packet.config.payload.PayloadData;
import be.darkkraft.transferproxy.util.BufUtil;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public record PluginMessagePacket(String channel, @Nullable PayloadData payload) implements ServerboundPacket {

    public static final Map<String, Function<ByteBuf, PayloadData>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("minecraft:brand", BrandPayload::new);
    }

    public static PluginMessagePacket from(final @NotNull ByteBuf buf) {
        final String channel = BufUtil.readString(buf);
        final Function<ByteBuf, PayloadData> function = REGISTRY.get(channel);
        return new PluginMessagePacket(channel, function != null ? function.apply(buf) : null);
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {

    }

    @Override
    public void write(final @NotNull ByteBuf buf) {
        BufUtil.writeString(buf, this.channel);
        if (this.payload != null) {
            this.payload.write(buf);
        }
    }

    @Override
    public int getId() {
        return 0x03;
    }

}