package be.darkkraft.transferproxy.network.packet.login.clientbound;

import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.api.profile.Property;
import be.darkkraft.transferproxy.util.BufUtil;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static be.darkkraft.transferproxy.util.BufUtil.writeString;
import static be.darkkraft.transferproxy.util.BufUtil.writeVarInt;

public record LoginSuccessPacket(UUID uuid, String username, Property[] properties) implements Packet {

    @Override
    public void write(final @NotNull ByteBuf buf) {
        BufUtil.writeUUID(buf, this.uuid);
        writeString(buf, this.username);
        if (this.properties == null) {
            writeVarInt(buf, 0);
            return;
        }
        writeVarInt(buf, this.properties.length);
        for (final Property property : this.properties) {
            writeString(buf, property.name());
            writeString(buf, property.value());
            final String signature = property.signature();
            if (signature != null && !signature.isEmpty()) {
                buf.writeBoolean(true);
                writeString(buf, signature);
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    @Override
    public int getId() {
        return 0x02;
    }

}