package be.darkkraft.transferproxy.api.network.packet.serverbound;

import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.Packet;
import org.jetbrains.annotations.NotNull;

public interface ServerboundPacket extends Packet {

    void handle(final @NotNull PlayerConnection connection);

}