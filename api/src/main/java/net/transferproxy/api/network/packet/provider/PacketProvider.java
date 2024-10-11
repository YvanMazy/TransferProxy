/*
 * MIT License
 *
 * Copyright (c) 2024 Yvan Mazy
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

package net.transferproxy.api.network.packet.provider;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.transferproxy.api.network.connection.ConnectionState;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.network.packet.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface PacketProvider {

    @Nullable Packet provide(final PlayerConnection connection, final @NotNull ByteBuf buf);

    static @Nullable Packet buildPacket(final @NotNull PlayerConnection connection,
                              final @NotNull ConnectionState state,
                              final @NotNull ByteBuf buf,
                              final int packetId) {
        final PacketProvider[] packets = connection.getPacketProviderGroup().getProviders(state);

        if (packets == null) {
            throw new DecoderException("Invalid packet 0x" + Integer.toHexString(packetId) + " on state: " + state);
        }

        if (packetId >= packets.length) {
            return null;
        }

        final PacketProvider provider = packets[packetId];
        final Packet packet;
        if (provider == null || (packet = provider.provide(connection, buf)) == null) {
            return null;
        }

        return packet;
    }

}