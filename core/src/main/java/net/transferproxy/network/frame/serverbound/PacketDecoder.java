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

package net.transferproxy.network.frame.serverbound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.network.connection.ConnectionState;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.api.network.packet.provider.PacketProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static net.transferproxy.util.BufUtil.readVarInt;

public final class PacketDecoder extends ByteToMessageDecoder {

    private static final boolean CHECK_EXTRA_BYTE = !TransferProxy.getInstance().getConfiguration().getNetwork().isDisableExtraByteCheck();

    private final PlayerConnection connection;

    public PacketDecoder(final @NotNull PlayerConnection connection) {
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
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
            final Packet packet = PacketProvider.buildPacket(this.connection, state, in, packetId);
            if (packet == null) {
                throw new DecoderException("Bad packet id 0x" + Integer.toHexString(packetId) + " in state: " + state);
            }

            if (CHECK_EXTRA_BYTE) {
                final int readable = in.readableBytes();
                if (readable > 0) {
                    final String packetName = packet.getClass().getSimpleName();
                    throw new DecoderException("Packet on " + state + " (" + packetName + ") extra bytes: " + readable);
                }
            }

            out.add(packet);
        }
    }

}