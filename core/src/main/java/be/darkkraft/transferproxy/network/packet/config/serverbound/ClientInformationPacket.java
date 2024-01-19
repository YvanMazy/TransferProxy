package be.darkkraft.transferproxy.network.packet.config.serverbound;

import be.darkkraft.transferproxy.api.TransferProxy;
import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import be.darkkraft.transferproxy.api.profile.ChatVisibility;
import be.darkkraft.transferproxy.api.profile.ClientInformation;
import be.darkkraft.transferproxy.api.profile.MainHand;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static be.darkkraft.transferproxy.util.BufUtil.readString;
import static be.darkkraft.transferproxy.util.BufUtil.readVarInt;

public record ClientInformationPacket(String locale, byte viewDistance, ChatVisibility chatVisibility, boolean chatColors,
                                      byte displayedSkinParts, MainHand mainHand, boolean enableTextFiltering,
                                      boolean allowServerListing) implements ServerboundPacket, ClientInformation {

    public ClientInformationPacket(final @NotNull ByteBuf buf) {
        this(readString(buf, 16),
                buf.readByte(),
                ChatVisibility.fromId(readVarInt(buf)),
                buf.readBoolean(),
                buf.readByte(),
                MainHand.fromId(readVarInt(buf)),
                buf.readBoolean(),
                buf.readBoolean());
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        connection.setInformation(this);
        TransferProxy.getInstance().getModuleManager().getLoginHandler().handle(connection);
    }

    @Override
    public void write(final @NotNull ByteBuf buf) {

    }

    @Override
    public int getId() {
        return 0x00;
    }

}