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

package net.transferproxy.api.network.packet.built;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.api.network.protocol.Protocolized;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a pre-built packet snapshot that has already been serialized and cannot be modified.
 * <p>
 * This type of packet must be sent using the {@link #get(ByteBufAllocator)} method rather than
 * standard write methods.
 * All inherited {@link Packet} encoding operations are disabled to enforce proper usage of the pre-serialized snapshot.
 * </p>
 */
public interface BuiltPacket extends Packet {

    /**
     * Returns a pre-serialized byte buffer containing this packet's data.
     * <p>
     * This is the primary method for accessing the packet's content.
     * The provided allocator may be used to create or copy the buffer depending on implementation requirements.
     * </p>
     *
     * @param allocator the byte buffer allocator to use for buffer operations (must not be {@code null})
     * @return a byte buffer containing the fully serialized packet data
     */
    ByteBuf get(final @NotNull ByteBufAllocator allocator);

    /**
     * @throws IllegalStateException always thrown to enforce usage of {@link #get(ByteBufAllocator)}
     * @deprecated Not supported for pre-built packets - use {@link #get(ByteBufAllocator)} instead
     */
    @Contract("_ -> fail")
    @Override
    default void write(final @NotNull ByteBuf buf) {
        throwIllegalState();
    }

    /**
     * @throws IllegalStateException always thrown to enforce usage of {@link #get(ByteBufAllocator)}
     * @deprecated Not supported for pre-built packets - use {@link #get(ByteBufAllocator)} instead
     */
    @Contract("_, _ -> fail")
    @Override
    default void write(final @Nullable Protocolized protocolized, final @NotNull ByteBuf buf) {
        throwIllegalState();
    }

    /**
     * @throws IllegalStateException always thrown as packet ID is already embedded in the serialized snapshot
     * @deprecated ID retrieval is unnecessary for pre-built packets
     */
    @Contract("-> fail")
    @Override
    default int getId() {
        throwIllegalState();
        return 0x00; // Unreachable - compiler placeholder
    }

    private static void throwIllegalState() {
        throw new IllegalStateException("A snapshot packet must be sent with the #get() method");
    }

}