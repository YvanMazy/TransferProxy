/*
 * MIT License
 *
 * Copyright (c) 2026 Yvan Mazy
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

/**
 * Represents a factory interface for creating {@link Packet} instances from network data.
 * <p>
 * Implementations of this functional interface are responsible for parsing byte buffers
 * and constructing protocol-specific packet objects.
 * </p>
 */
@FunctionalInterface
public interface PacketProvider {

    /**
     * Creates a packet instance by parsing data from the provided byte buffer.
     *
     * @param connection the player connection context providing protocol state
     * @param buf the byte buffer containing raw packet data (must not be {@code null})
     * @return the parsed packet instance, or {@code null} if the packet cannot be created
     */
    @Nullable Packet provide(final PlayerConnection connection, final @NotNull ByteBuf buf);

    /**
     * Attempts to construct a packet using registered providers for a specific protocol state.
     *
     * @param connection the active player connection context (must not be {@code null})
     * @param state the current protocol state (must not be {@code null})
     * @param buf the byte buffer containing packet data (must not be {@code null})
     * @param packetId the numeric identifier of the packet being decoded
     * @return the parsed packet instance, or {@code null} if the packet ID is invalid or
     *         no provider exists for the ID
     * @throws DecoderException if no providers are registered for the specified protocol state
     * @throws NullPointerException if any non-null parameter is null
     */
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