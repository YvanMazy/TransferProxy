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

package net.transferproxy.test.agent.callback;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.players.NameAndId;
import net.transferproxy.test.common.SimpleStatusResponse;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StatusResponseCallback implements Runnable {

    private final ServerData serverData;
    private final CompletableFuture<SimpleStatusResponse> future;

    public StatusResponseCallback(final ServerData serverData, final CompletableFuture<SimpleStatusResponse> future) {
        this.serverData = serverData;
        this.future = future;
    }

    @Override
    public void run() {
        try {
            final String description = this.serverData.motd.getString();
            final SimpleStatusResponse.Players responsePlayers = this.getPlayers();
            final String favicon = this.getFavicon();

            final String name = this.serverData.version.getString();
            final int protocol = this.serverData.protocol;
            final SimpleStatusResponse.Version version = new SimpleStatusResponse.Version(name, protocol);

            this.future.complete(new SimpleStatusResponse(description, responsePlayers, version, favicon));
        } catch (final Throwable throwable) {
            this.future.completeExceptionally(throwable);
        }
    }

    private SimpleStatusResponse.Players getPlayers() {
        final ServerStatus.Players players = this.serverData.players;
        if (players != null) {
            final List<SimpleStatusResponse.Players.SampleEntry> list = new ArrayList<>();
            for (final NameAndId profile : players.sample()) {
                list.add(new SimpleStatusResponse.Players.SampleEntry(profile.name(), profile.id()));
            }
            return new SimpleStatusResponse.Players(players.online(),
                    players.max(),
                    list.toArray(new SimpleStatusResponse.Players.SampleEntry[0]));
        }
        return null;
    }

    private @Nullable String getFavicon() {
        if (this.serverData.getIconBytes() != null) {
            final String content = new String(Base64.getEncoder().encode(this.serverData.getIconBytes()), StandardCharsets.UTF_8);
            return "data:image/png;base64," + content;
        }
        return null;
    }

}