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

import net.transferproxy.api.network.connection.ConnectionState;
import net.transferproxy.api.network.packet.Packet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the packet layout used by a range of protocol versions.
 * <p>
 * A group defines how serverbound packets are decoded and which IDs are used to encode clientbound packets.
 * </p>
 */
@FunctionalInterface
public interface PacketProviderGroup {

    /**
     * Returns the serverbound packet providers for the given connection state, indexed by packet ID.
     *
     * @param state the connection state (must not be {@code null})
     * @return the providers indexed by packet ID, or {@code null} if no packet can be decoded in this state
     */
    PacketProvider @Nullable [] getProviders(final @NotNull ConnectionState state);

    /**
     * Returns the ID used to encode the given clientbound packet.
     * <p>
     * This default implementation delegates to {@link Packet#getId()}, which corresponds to the latest supported protocol.
     * Groups of older protocols should override this method for packets whose ID has changed.
     * </p>
     *
     * @param packet the clientbound packet to encode (must not be {@code null})
     * @return the packet ID in this group
     */
    @Contract(pure = true)
    default int getPacketId(final @NotNull Packet packet) {
        return packet.getId();
    }

}