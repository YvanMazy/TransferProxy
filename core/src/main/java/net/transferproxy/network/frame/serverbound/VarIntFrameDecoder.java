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

package net.transferproxy.network.frame.serverbound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

public final class VarIntFrameDecoder extends ByteToMessageDecoder {

    private int packetLength = -1;

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        if (this.packetLength == -1) {
            in.markReaderIndex();
            int tmpPacketLength = 0;
            for (int i = 0; i < 3; ++i) {
                if (!in.isReadable()) {
                    in.resetReaderIndex();
                    return;
                }
                final int part = in.readByte();
                tmpPacketLength |= (part & 0x7F) << (i * 7);
                if (part >= 0) {
                    this.packetLength = tmpPacketLength;
                    if (this.packetLength == 0) {
                        this.packetLength = -1;
                    }
                    return;
                }
            }
            throw new CorruptedFrameException("Packet length VarInt length is more than 21 bits");
        }
        if (in.readableBytes() < this.packetLength) {
            return;
        }
        out.add(in.readRetainedSlice(this.packetLength));
        this.packetLength = -1;
    }

}