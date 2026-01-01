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

package net.transferproxy.status;

import net.transferproxy.api.event.listener.StatusListener;
import net.transferproxy.api.event.status.StatusRequestEvent;
import net.transferproxy.api.network.packet.built.ProtocolizedBuiltPacket;
import net.transferproxy.api.status.StatusResponse;
import net.transferproxy.api.util.ComponentProtocolUtil;
import net.transferproxy.network.packet.built.ProtocolizedBuiltPacketImpl;
import net.transferproxy.network.packet.status.clientbound.StatusResponsePacket;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SnapshotStatusListener implements StatusListener {

    private final ProtocolizedBuiltPacket builtPacket;

    public SnapshotStatusListener(final @NotNull StatusResponse response) {
        Objects.requireNonNull(response, "response must not be null");
        final StatusResponsePacket packet = new StatusResponsePacket(response);
        this.builtPacket = new ProtocolizedBuiltPacketImpl(packet, true, ComponentProtocolUtil.getSerializerProtocols());
    }

    @Override
    public void handle(final @NotNull StatusRequestEvent event) {
        event.setCanSendResponsePacket(false);
        event.getConnection().sendPacket(this.builtPacket);
    }

}