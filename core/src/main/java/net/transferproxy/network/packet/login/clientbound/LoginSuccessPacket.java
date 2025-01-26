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

package net.transferproxy.network.packet.login.clientbound;

import io.netty.buffer.ByteBuf;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.api.network.protocol.Protocolized;
import net.transferproxy.api.profile.Property;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static net.transferproxy.util.BufUtil.*;

public record LoginSuccessPacket(UUID uuid, String username, Property[] properties) implements Packet {

    public LoginSuccessPacket(final @NotNull PlayerConnection connection, final @NotNull ByteBuf buf) {
        this(readUUID(buf),
                readString(buf, 16),
                readArray(buf,
                        Property[]::new,
                        sub -> new Property(readString(sub), readString(sub), sub.readBoolean() ? readString(buf) : null),
                        16));
        if (connection.getProtocol() < 768) { // 768 = 1.21.2
            buf.readBoolean();
        }
    }

    @Override
    public void write(final @NotNull Protocolized protocolized, final @NotNull ByteBuf buf) {
        writeUUID(buf, this.uuid);
        writeString(buf, this.username, 16);
        if (this.properties == null) {
            writeVarInt(buf, 0);
        } else {
            writeArray(buf, this.properties, (sub, property) -> {
                writeString(sub, property.name());
                writeString(sub, property.value());
                final String signature = property.signature();
                if (signature != null && !signature.isEmpty()) {
                    sub.writeBoolean(true);
                    writeString(sub, signature);
                } else {
                    sub.writeBoolean(false);
                }
            });
        }
        if (protocolized.getProtocol() < 768) { // 768 = 1.21.2
            buf.writeBoolean(true);
        }
    }

    @Override
    public int getId() {
        return 0x02;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final LoginSuccessPacket that = (LoginSuccessPacket) o;
        return Objects.equals(this.uuid, that.uuid) && Objects.equals(this.username, that.username) &&
                Arrays.equals(this.properties, that.properties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.uuid, this.username);
        result = 31 * result + Arrays.hashCode(this.properties);
        return result;
    }

    @Override
    public String toString() {
        return "LoginSuccessPacket{uuid=" + this.uuid + ", username='" + this.username + "', properties=" +
                Arrays.toString(this.properties) + '}';
    }

}