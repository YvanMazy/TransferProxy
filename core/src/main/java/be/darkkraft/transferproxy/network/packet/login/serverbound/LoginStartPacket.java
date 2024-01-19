package be.darkkraft.transferproxy.network.packet.login.serverbound;

import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import be.darkkraft.transferproxy.network.packet.login.clientbound.LoginSuccessPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static be.darkkraft.transferproxy.util.BufUtil.*;

public record LoginStartPacket(String name, UUID uuid) implements ServerboundPacket {

    public LoginStartPacket(final @NotNull ByteBuf buf) {
        this(readString(buf), readUUID(buf));
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        connection.setProfile(this.name, this.uuid);
        connection.sendPacket(new LoginSuccessPacket(this.uuid, this.name, null));
    }

    @Override
    public void write(final @NotNull ByteBuf buf) {
        writeString(buf, this.name);
        writeUUID(buf, this.uuid);
    }

    @Override
    public int getId() {
        return 0x00;
    }

}