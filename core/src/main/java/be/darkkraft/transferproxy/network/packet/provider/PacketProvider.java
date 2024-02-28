/*
 * MIT License
 *
 * Copyright (c) 2024 Darkkraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package be.darkkraft.transferproxy.network.packet.provider;

import be.darkkraft.transferproxy.api.network.connection.ConnectionState;
import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.network.packet.config.FinishConfigurationPacket;
import be.darkkraft.transferproxy.network.packet.config.KeepAlivePacket;
import be.darkkraft.transferproxy.network.packet.config.PluginMessagePacket;
import be.darkkraft.transferproxy.network.packet.config.serverbound.ClientInformationPacket;
import be.darkkraft.transferproxy.network.packet.config.serverbound.ConfigCookieResponsePacket;
import be.darkkraft.transferproxy.network.packet.config.serverbound.ResourcePackResponsePacket;
import be.darkkraft.transferproxy.network.packet.handshake.HandshakePacket;
import be.darkkraft.transferproxy.network.packet.login.serverbound.LoginAcknowledgedPacket;
import be.darkkraft.transferproxy.network.packet.login.serverbound.LoginCookieResponsePacket;
import be.darkkraft.transferproxy.network.packet.login.serverbound.LoginStartPacket;
import be.darkkraft.transferproxy.network.packet.status.PingPongPacket;
import be.darkkraft.transferproxy.network.packet.status.serverbound.StatusRequestPacket;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import org.jetbrains.annotations.NotNull;

public interface PacketProvider {

    PacketProvider[] HANDSHAKE = {HandshakePacket::new};
    PacketProvider[] STATUS = {StatusRequestPacket::new, PingPongPacket::new};
    PacketProvider[] LOGIN = {LoginStartPacket::new, null, null, LoginAcknowledgedPacket::new, LoginCookieResponsePacket::new};
    PacketProvider[] CONFIG =
            {ClientInformationPacket::new, ConfigCookieResponsePacket::new, PluginMessagePacket::from, FinishConfigurationPacket::new, KeepAlivePacket::new, null, ResourcePackResponsePacket::new};

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

    static PacketProvider[] getProviders(final @NotNull ConnectionState state) {
        return switch (state) {
            case HANDSHAKE -> HANDSHAKE;
            case STATUS -> STATUS;
            case LOGIN -> LOGIN;
            case CONFIG -> CONFIG;
            default -> null;
        };
    }

}