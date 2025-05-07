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

package net.transferproxy.status;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.configuration.ProxyConfiguration;
import net.transferproxy.api.event.EventType;
import net.transferproxy.api.event.status.StatusRequestEvent;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.network.packet.built.ProtocolizedBuiltPacket;
import net.transferproxy.api.status.StatusManager;
import net.transferproxy.api.status.StatusResponse;
import net.transferproxy.api.util.ComponentProtocolUtil;
import net.transferproxy.api.util.IOUtil;
import net.transferproxy.network.packet.built.ProtocolizedBuiltPacketImpl;
import net.transferproxy.network.packet.status.clientbound.StatusResponsePacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StatusManagerImpl implements StatusManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusManagerImpl.class);

    private final String name;
    private final Component description;
    private final int protocol;
    private final boolean autoProtocol;
    private final int online;
    private final int maxOnline;
    private final String favicon;

    private ProtocolizedBuiltPacket builtResponse;

    public StatusManagerImpl() {
        final ProxyConfiguration.Status config = TransferProxy.getInstance().getConfiguration().getStatus();
        this.name = config.getName();
        this.description = MiniMessage.miniMessage().deserialize(config.getDescription());
        final String rawProtocol = config.getProtocol();
        if (rawProtocol.equalsIgnoreCase("auto")) {
            this.autoProtocol = true;
            this.protocol = -1;
        } else {
            this.autoProtocol = false;
            this.protocol = this.parseProtocol(rawProtocol);
        }
        this.online = config.getOnline();
        this.maxOnline = config.getMaxOnline();
        this.favicon = this.readFavicon(Path.of(config.getFaviconPath()));

        if (!this.autoProtocol) {
            final StatusResponsePacket packet = new StatusResponsePacket(this.buildDefaultResponse(-1));
            this.builtResponse = new ProtocolizedBuiltPacketImpl(packet, true, ComponentProtocolUtil.getSerializerProtocols());
        }
    }

    @Override
    public void process(final @NotNull PlayerConnection connection) {
        final StatusRequestEvent event = new StatusRequestEvent(connection);
        TransferProxy.getInstance().getModuleManager().getEventManager().call(EventType.STATUS, event);
        if (event.canSendResponsePacket()) {
            StatusResponse response = event.getResponse();
            if (response == null) {
                if (this.builtResponse != null) {
                    connection.sendPacket(this.builtResponse);
                    return;
                }
                response = this.buildDefaultResponse(event.getConnection().getProtocol());
            }
            connection.sendStatusResponse(response);
        }
    }

    @Override
    public @NotNull StatusResponse buildDefaultResponse(final int protocol) {
        return StatusResponse.builder()
                .name(this.name)
                .description(this.description)
                .protocol(this.autoProtocol ? protocol : this.protocol)
                .favicon(this.favicon)
                .online(this.online)
                .max(this.maxOnline)
                .build();
    }

    private int parseProtocol(final @NotNull String rawProtocol) {
        try {
            return Integer.parseInt(rawProtocol);
        } catch (final NumberFormatException exception) {
            throw new NumberFormatException("Status protocol must be a number: " + rawProtocol);
        }
    }

    private @Nullable String readFavicon(final Path path) {
        if (Files.isRegularFile(path)) {
            try {
                return IOUtil.createImage(path);
            } catch (final IOException e) {
                LOGGER.error("Failed to read favicon", e);
            }
        }
        return null;
    }

}