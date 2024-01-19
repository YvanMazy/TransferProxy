package be.darkkraft.transferproxy.network.packet.status.clientbound;

import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.api.status.response.StatusResponse;
import be.darkkraft.transferproxy.util.BufUtil;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson;

public record StatusResponsePacket(StatusResponse response) implements Packet {

    @Override
    public void write(final @NotNull ByteBuf buf) {
        BufUtil.writeString(buf, gson().serializer().toJson(this.response));
    }

    @Override
    public int getId() {
        return 0x00;
    }

}