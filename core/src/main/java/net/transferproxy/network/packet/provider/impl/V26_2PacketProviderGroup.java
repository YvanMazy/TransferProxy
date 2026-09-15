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

package net.transferproxy.network.packet.provider.impl;

import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.network.packet.config.clientbound.CodeOfConductPacket;
import net.transferproxy.network.packet.config.clientbound.ServerSelectKnownPacksPacket;
import net.transferproxy.network.packet.config.clientbound.StoreCookiePacket;
import net.transferproxy.network.packet.config.clientbound.TransferPacket;
import org.jetbrains.annotations.NotNull;

public final class V26_2PacketProviderGroup extends LatestPacketProviderGroup {

    @Override
    public int getPacketId(final @NotNull Packet packet) {
        return switch (packet) {
            case StoreCookiePacket _ -> 0x0A;
            case TransferPacket _ -> 0x0B;
            case ServerSelectKnownPacksPacket _ -> 0x0E;
            case CodeOfConductPacket _ -> 0x13;
            default -> super.getPacketId(packet);
        };
    }

}