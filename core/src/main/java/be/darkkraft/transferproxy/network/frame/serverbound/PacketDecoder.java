package be.darkkraft.transferproxy.network.frame.serverbound;

import be.darkkraft.transferproxy.api.network.connection.ConnectionState;
import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.network.packet.provider.PacketProvider;
import be.darkkraft.transferproxy.util.BufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

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

            final int packetId = BufUtil.readVarInt(in);
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