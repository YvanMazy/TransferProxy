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

package be.darkkraft.transferproxy.api.status.listener;

import be.darkkraft.transferproxy.api.TransferProxy;
import be.darkkraft.transferproxy.api.configuration.ProxyConfiguration;
import be.darkkraft.transferproxy.api.event.listener.StatusListener;
import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.status.StatusResponse;
import be.darkkraft.transferproxy.api.util.IOUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DefaultStatusListener implements StatusListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStatusListener.class);

    private final String name;
    private final Component description;
    private final int protocol;
    private final boolean autoProtocol;
    private final String favicon;

    public DefaultStatusListener() {
        final ProxyConfiguration.Status config = TransferProxy.getInstance().getConfiguration().getStatus();
        this.name = config.getName();
        this.description = MiniMessage.miniMessage().deserialize(config.getDescription());
        final String rawProtocol = config.getProtocol();
        if (rawProtocol.equalsIgnoreCase("auto")) {
            this.autoProtocol = true;
            this.protocol = -1;
        } else {
            this.autoProtocol = false;
            this.protocol = parseProtocol(rawProtocol);
        }
        this.favicon = readFavicon(Path.of(config.getFaviconPath()));
    }

    @Override
    public void handle(final @NotNull PlayerConnection connection) {
        connection.sendStatusResponse(StatusResponse.builder()
                .name(this.name)
                .description(this.description)
                .protocol(this.autoProtocol ? connection.getProtocol() : this.protocol)
                .favicon(this.favicon)
                .build());
    }

    private static int parseProtocol(final @NotNull String rawProtocol) {
        try {
            return Integer.parseInt(rawProtocol);
        } catch (final NumberFormatException exception) {
            throw new NumberFormatException("Status protocol must be a number: " + rawProtocol);
        }
    }

    private static @Nullable String readFavicon(final Path path) {
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