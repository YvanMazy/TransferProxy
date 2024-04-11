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

package be.darkkraft.transferproxy.network.packet.pack;

import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.api.resourcepack.KnownPack;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static be.darkkraft.transferproxy.util.BufUtil.*;

public abstract class SelectKnownPacksPacket implements Packet {

    private final List<KnownPack> packs;

    protected SelectKnownPacksPacket(final List<KnownPack> packs) {
        this.packs = packs;
    }

    protected static <T> T from(final @NotNull ByteBuf buf, final @NotNull Function<List<KnownPack>, T> function) {
        final int size = readVarInt(buf);
        if (size > 64) {
            throw new DecoderException("Too many known packs found: " + size);
        }
        final List<KnownPack> packs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final String namespace = readString(buf);
            final String id = readString(buf);
            final String version = readString(buf);
            packs.add(new KnownPack(namespace, id, version));
        }

        return function.apply(packs);
    }

    @Override
    public void write(final @NotNull ByteBuf buf) {
        final int size = this.packs.size();
        writeVarInt(buf, size);
        for (final KnownPack pack : this.packs) {
            writeString(buf, pack.namespace());
            writeString(buf, pack.id());
            writeString(buf, pack.version());
        }
    }

    public List<KnownPack> packs() {
        return this.packs;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final var that = (SelectKnownPacksPacket) obj;
        return Objects.equals(this.packs, that.packs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.packs);
    }

    @Override
    public String toString() {
        return "SelectKnownPackPacket[packs=" + this.packs + ']';
    }

}