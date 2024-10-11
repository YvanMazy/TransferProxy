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

package net.transferproxy.network.packet.config.clientbound;

import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.api.util.CookieUtil;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

import static net.transferproxy.util.BufUtil.*;

public record StoreCookiePacket(String key, byte[] payload) implements Packet {

    public StoreCookiePacket(final @NotNull ByteBuf buf) {
        this(readString(buf), readBytes(buf, CookieUtil.getMaxCookieSize()));
    }

    @Override
    public void write(final @Nullable PlayerConnection connection, final @NotNull ByteBuf buf) {
        writeString(buf, this.key);
        writeBytes(buf, this.payload, CookieUtil.getMaxCookieSize());
    }

    @Override
    public int getId() {
        return 0x0A;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final StoreCookiePacket that = (StoreCookiePacket) o;
        return Objects.equals(this.key, that.key) && Arrays.equals(this.payload, that.payload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.key);
        result = 31 * result + Arrays.hashCode(this.payload);
        return result;
    }

    @Override
    public String toString() {
        return "StoreCookiePacket{key='" + this.key + "', payload=" + Arrays.toString(this.payload) + '}';
    }

}