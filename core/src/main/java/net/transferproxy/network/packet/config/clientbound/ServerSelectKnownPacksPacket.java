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

package net.transferproxy.network.packet.config.clientbound;

import io.netty.buffer.ByteBuf;
import net.transferproxy.api.resourcepack.KnownPack;
import net.transferproxy.network.packet.pack.SelectKnownPacksPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ServerSelectKnownPacksPacket extends SelectKnownPacksPacket {

    public ServerSelectKnownPacksPacket(final @NotNull List<KnownPack> packs) {
        super(packs);
    }

    public static ServerSelectKnownPacksPacket from(final @NotNull ByteBuf buf) {
        return from(buf, ServerSelectKnownPacksPacket::new);
    }

    @Override
    public int getId() {
        return 0x0E;
    }

}