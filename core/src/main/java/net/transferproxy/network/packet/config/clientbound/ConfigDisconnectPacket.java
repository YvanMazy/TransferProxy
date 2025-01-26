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

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.api.network.protocol.Protocolized;
import net.transferproxy.api.util.ComponentProtocolUtil;
import net.transferproxy.util.BufUtil;
import net.transferproxy.util.NBTUtil;
import org.jetbrains.annotations.NotNull;

public record ConfigDisconnectPacket(Component component) implements Packet {

    public ConfigDisconnectPacket(final int protocol, final @NotNull ByteBuf buf) {
        this(ComponentProtocolUtil.getSerializer(protocol).deserializeFromTree(NBTUtil.deserialize(BufUtil.readTag(buf))));
    }

    @Override
    public void write(final @NotNull Protocolized protocolized, final @NotNull ByteBuf buf) {
        final JsonElement tree = ComponentProtocolUtil.getSerializer(protocolized.getProtocol()).serializeToTree(this.component);
        BufUtil.writeTag(buf, NBTUtil.serialize(tree));
    }

    @Override
    public int getId() {
        return 0x02;
    }

}