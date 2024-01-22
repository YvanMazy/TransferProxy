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

package be.darkkraft.transferproxy.network.frame.serverbound;

import be.darkkraft.transferproxy.api.network.connection.ConnectionState;
import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.network.packet.provider.PacketProvider;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static be.darkkraft.transferproxy.util.BufUtil.readVarInt;

public final class PacketDecoder extends ByteToMessageDecoder {

    private final PlayerConnection connection;

    public PacketDecoder(final @NotNull PlayerConnection connection) {
        this.connection = Objects.requireNonNull(connection, "Connection cannot be null");
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        final int i = in.readableBytes();

        if (i != 0) {
            final ConnectionState state = this.connection.getState();
            if (state == ConnectionState.CLOSED) {
                return;
            }

            final int packetId = readVarInt(in);
            final Packet packet = PacketProvider.buildPacket(state, in, packetId);
            if (packet == null) {
                throw new DecoderException("Bad packet id 0x" + Integer.toHexString(packetId) + " in state: " + state);
            }

            final int readable = in.readableBytes();
            if (readable > 0) {
                final String packetName = packet.getClass().getSimpleName();
                throw new DecoderException("Packet on " + state + " (" + packetName + ") extra bytes: " + readable);
            }

            out.add(packet);
        }
    }

}