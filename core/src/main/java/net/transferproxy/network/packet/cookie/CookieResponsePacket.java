/*
 * MIT License
 *
 * Copyright (c) 2026 Yvan Mazy
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

package net.transferproxy.network.packet.cookie;

import io.netty.buffer.ByteBuf;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import net.transferproxy.api.network.protocol.Protocolized;
import net.transferproxy.api.util.CookieUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

import static net.transferproxy.util.BufUtil.*;

public abstract class CookieResponsePacket implements ServerboundPacket {

    private final String key;
    private final byte[] payload;

    protected CookieResponsePacket(final String key, final byte @Nullable [] payload) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.payload = payload;
    }

    protected CookieResponsePacket(final @NotNull ByteBuf buf) {
        this(readString(buf), buf.readBoolean() ? readBytes(buf, CookieUtil.getMaxCookieSize()) : null);
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        connection.handleCookieResponse(this.key, this.payload);
    }

    @Override
    public void write(final @NotNull Protocolized protocolized, final @NotNull ByteBuf buf) {
        writeString(buf, this.key);
        if (this.payload != null) {
            buf.writeBoolean(true);
            writeBytes(buf, this.payload, CookieUtil.getMaxCookieSize());
            return;
        }
        buf.writeBoolean(false);
    }

    public String key() {
        return this.key;
    }

    public byte @Nullable [] payload() {
        return this.payload;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final CookieResponsePacket that = (CookieResponsePacket) o;
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
        return "CookieResponsePacket{key='" + this.key + '\'' + ", payload=" + Arrays.toString(this.payload) + '}';
    }

}