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

package net.transferproxy.network.packet.config.clientbound;

import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.api.network.packet.built.BuiltPacket;
import net.transferproxy.network.packet.built.BuiltPacketImpl;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ResetChatPacket() implements Packet {

    public static final BuiltPacket INSTANCE = new BuiltPacketImpl(new ResetChatPacket());

    public ResetChatPacket(@SuppressWarnings("unused") final @NotNull ByteBuf buf) {
        this();
    }

    @Override
    public void write(final @Nullable PlayerConnection connection, final @NotNull ByteBuf buf) {
        // do nothing
    }

    @Override
    public int getId() {
        return 0x06;
    }

}