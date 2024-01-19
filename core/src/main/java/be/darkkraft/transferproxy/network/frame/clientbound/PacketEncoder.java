package be.darkkraft.transferproxy.network.frame.clientbound;

import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.util.BufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public final class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Packet msg, final ByteBuf out) {
        BufUtil.writeVarInt(out, msg.getId());
        msg.write(out);
    }

}