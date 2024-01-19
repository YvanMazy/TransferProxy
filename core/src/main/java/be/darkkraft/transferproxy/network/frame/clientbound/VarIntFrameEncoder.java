package be.darkkraft.transferproxy.network.frame.clientbound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static be.darkkraft.transferproxy.util.BufUtil.writeVarInt;

@ChannelHandler.Sharable
public final class VarIntFrameEncoder extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf buf, final ByteBuf out) {
        writeVarInt(out, buf.readableBytes());
        out.writeBytes(buf);
    }

}