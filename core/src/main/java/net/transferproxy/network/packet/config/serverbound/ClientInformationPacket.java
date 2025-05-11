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

package net.transferproxy.network.packet.config.serverbound;

import io.netty.buffer.ByteBuf;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.event.EventType;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import net.transferproxy.api.network.protocol.Protocolized;
import net.transferproxy.api.profile.ChatVisibility;
import net.transferproxy.api.profile.ClientInformation;
import net.transferproxy.api.profile.MainHand;
import net.transferproxy.api.profile.ParticleStatus;
import org.jetbrains.annotations.NotNull;

import static net.transferproxy.util.BufUtil.*;

public record ClientInformationPacket(String locale, byte viewDistance, ChatVisibility chatVisibility, boolean chatColors,
                                      byte displayedSkinParts, MainHand mainHand, boolean enableTextFiltering, boolean allowServerListing,
                                      ParticleStatus particleStatus) implements ServerboundPacket, ClientInformation {

    public ClientInformationPacket(final @NotNull ClientInformation information) {
        this(information.locale(),
                information.viewDistance(),
                information.chatVisibility(),
                information.chatColors(),
                information.displayedSkinParts(),
                information.mainHand(),
                information.enableTextFiltering(),
                information.allowServerListing(),
                information.particleStatus());
    }

    public ClientInformationPacket(final @NotNull PlayerConnection connection, final @NotNull ByteBuf buf) {
        this(readString(buf, 16),
                buf.readByte(),
                ChatVisibility.fromId(readVarInt(buf)),
                buf.readBoolean(),
                buf.readByte(),
                MainHand.fromId(readVarInt(buf)),
                buf.readBoolean(),
                buf.readBoolean(),
                connection.getProtocol() >= 768 ? ParticleStatus.fromId(readVarInt(buf)) : null); // 768 = 1.21.1
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        connection.setInformation(this);
        TransferProxy.getInstance().getModuleManager().getEventManager().call(EventType.READY, connection);
    }

    @Override
    public void write(final @NotNull Protocolized protocolized, final @NotNull ByteBuf buf) {
        writeString(buf, this.locale);
        buf.writeByte(this.viewDistance);
        writeVarInt(buf, this.chatVisibility.getId());
        buf.writeBoolean(this.chatColors);
        buf.writeByte(this.displayedSkinParts);
        writeVarInt(buf, this.mainHand.ordinal());
        buf.writeBoolean(this.enableTextFiltering);
        buf.writeBoolean(this.allowServerListing);
        if (protocolized.getProtocol() >= 768) { // 768 = 1.21.1
            writeVarInt(buf, this.particleStatus.getId());
        }
    }

    @Override
    public int getId() {
        return 0x00;
    }

}