package be.darkkraft.transferproxy.network.packet.provider;

import be.darkkraft.transferproxy.api.network.connection.ConnectionState;
import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.network.packet.config.FinishConfigurationPacket;
import be.darkkraft.transferproxy.network.packet.config.PluginMessagePacket;
import be.darkkraft.transferproxy.network.packet.config.serverbound.ClientInformationPacket;
import be.darkkraft.transferproxy.network.packet.handshake.HandshakePacket;
import be.darkkraft.transferproxy.network.packet.login.serverbound.LoginAcknowledgedPacket;
import be.darkkraft.transferproxy.network.packet.login.serverbound.LoginStartPacket;
import be.darkkraft.transferproxy.network.packet.status.PingPongPacket;
import be.darkkraft.transferproxy.network.packet.status.serverbound.StatusRequestPacket;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import org.jetbrains.annotations.NotNull;

public interface PacketProvider {

    PacketProvider[] HANDSHAKE = {HandshakePacket::new};
    PacketProvider[] STATUS = {StatusRequestPacket::new, PingPongPacket::new};
    PacketProvider[] LOGIN = {LoginStartPacket::new, null, null, LoginAcknowledgedPacket::new};
    PacketProvider[] CONFIG = {ClientInformationPacket::new, null, PluginMessagePacket::from, FinishConfigurationPacket::new};

    Packet provide(final @NotNull ByteBuf buf);

    static Packet buildPacket(final @NotNull ConnectionState state, final @NotNull ByteBuf buf, final int packetId) {
        final PacketProvider[] packets = getProviders(state);

        if (packets == null) {
            throw new DecoderException("Invalid packet 0x" + Integer.toHexString(packetId) + " on state: " + state);
        }

        if (packetId >= packets.length) {
            return null;
        }

        final PacketProvider provider = packets[packetId];
        final Packet packet;
        if (provider == null || (packet = provider.provide(buf)) == null) {
            return null;
        }

        return packet;
    }

    private static PacketProvider[] getProviders(final @NotNull ConnectionState state) {
        return switch (state) {
            case HANDSHAKE -> HANDSHAKE;
            case STATUS -> STATUS;
            case LOGIN -> LOGIN;
            case CONFIG -> CONFIG;
            default -> null;
        };
    }

    private static ProviderSupplier of(final int id, final @NotNull PacketProvider provider) {
        return new ProviderSupplier(id, provider);
    }

    record ProviderSupplier(int id, @NotNull PacketProvider provider) {

    }

}