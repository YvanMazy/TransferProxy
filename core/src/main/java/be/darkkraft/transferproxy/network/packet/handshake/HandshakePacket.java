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

package be.darkkraft.transferproxy.network.packet.handshake;

import be.darkkraft.transferproxy.api.TransferProxy;
import be.darkkraft.transferproxy.api.configuration.ProxyConfiguration;
import be.darkkraft.transferproxy.api.event.EventType;
import be.darkkraft.transferproxy.api.network.connection.ConnectionState;
import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.api.network.packet.built.BuiltPacket;
import be.darkkraft.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import be.darkkraft.transferproxy.network.packet.built.BuiltPacketImpl;
import be.darkkraft.transferproxy.network.packet.login.clientbound.LoginDisconnectPacket;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import static be.darkkraft.transferproxy.util.BufUtil.*;

public record HandshakePacket(int protocol, String hostname, int hostPort, ConnectionState nextState) implements ServerboundPacket {

    private static BuiltPacket kickPacket;

    public HandshakePacket(final @NotNull ByteBuf buf) {
        this(readVarInt(buf), readString(buf), buf.readShort(), ConnectionState.fromId(readVarInt(buf)));
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        connection.setProtocol(this.protocol);
        connection.setHost(this.hostname, this.hostPort);
        connection.setState(this.nextState);
        final TransferProxy proxy = TransferProxy.getInstance();
        if (this.protocol < 766) {
            final ProxyConfiguration.Miscellaneous config = proxy.getConfiguration().getMiscellaneous();
            if (config.isKickOldProtocol()) {
                connection.sendPacketAndClose(getKickPacket());
                return;
            }
        }
        proxy.getModuleManager().getEventManager().call(EventType.HANDSHAKE, connection);
    }

    @Override
    public void write(final @NotNull ByteBuf buf) {
        writeVarInt(buf, this.protocol);
        writeString(buf, this.hostname);
        buf.writeShort(this.hostPort);
        writeVarInt(buf, this.nextState.ordinal());
    }

    @Override
    public int getId() {
        return 0x00;
    }

    private static Packet getKickPacket() {
        if (kickPacket != null) {
            return kickPacket;
        }
        return kickPacket = new BuiltPacketImpl(new LoginDisconnectPacket(MiniMessage.miniMessage()
                .deserialize(TransferProxy.getInstance().getConfiguration().getMiscellaneous().getKickOldProtocolMessage())));
    }

}