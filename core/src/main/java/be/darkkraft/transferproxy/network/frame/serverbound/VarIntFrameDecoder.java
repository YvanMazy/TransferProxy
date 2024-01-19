package be.darkkraft.transferproxy.network.frame.serverbound;

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