/*
 * MIT License
 *
 * Copyright (c) 2025 Yvan Mazy
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

package net.transferproxy.api.network.packet;

import io.netty.buffer.ByteBuf;
import net.transferproxy.api.network.protocol.Protocolized;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a network packet that can be encoded into a byte buffer for transmission.
 * <p>
 * Implementing classes should define how packet data is written to a buffer, optionally using
 * a player connection context when necessary.
 * Packets are typically identified by a unique integer ID used during network communication.
 * </p>
 */
public interface Packet {

    /**
     * Writes this packet's data to the specified byte buffer without a player connection context.
     * <p>
     * This default implementation delegates to {@link #write(Protocolized, ByteBuf)} with
     * a {@code null} connection parameter.
     * Implementations should override this method if connection-agnostic encoding requires different logic.
     * </p>
     *
     * @param buf the target byte buffer to write data to (must not be {@code null})
     */
    default void write(final @NotNull ByteBuf buf) {
        this.write(Protocolized.empty(), buf);
    }

    /**
     * Writes this packet's data to the specified byte buffer, optionally using a player connection
     * for context-dependent encoding.
     * <p>
     * Examples of connection-dependent logic include version-specific formatting or
     * protocol translation.
     * Implementations must handle {@code null} connection values appropriately.
     * </p>
     * Natively, connection can be null when the packet is converter to a snapshot.
     *
     * @param protocolized the context (maybe {@code null} if irrelevant)
     * @param buf the target byte buffer to write data to (must not be {@code null})
     */
    void write(final @NotNull Protocolized protocolized, final @NotNull ByteBuf buf);

    /**
     * Returns the unique numeric identifier for this packet type.
     * <p>
     * This ID is used by the network protocol to recognize and route packets correctly.
     * </p>
     *
     * @return the packet's network protocol ID
     */
    @Contract(pure = true)
    int getId();

}