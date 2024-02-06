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

package be.darkkraft.transferproxy.api.network.connection;

import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.api.profile.ClientInformation;
import be.darkkraft.transferproxy.api.status.StatusResponse;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerConnection {

    void transfer(final @NotNull String host, final int hostPort);

    void sendLoginSuccess(final @NotNull UUID uuid, final @NotNull String username);

    void sendStatusResponse(final @NotNull StatusResponse response);

    CompletableFuture<byte[]> fetchCookie(final @NotNull String cookieKey);

    void storeCookie(final @NotNull String cookieKey, final byte @NotNull [] payload);

    void handleCookieResponse(final @NotNull String cookieKey, final byte @Nullable [] payload);

    Map<String, CompletableFuture<byte[]>> getPendingCookies();

    default void disconnect(final @NotNull String reason) {
        this.disconnect(Component.text(reason));
    }

    void disconnect(final @NotNull Component reason);

    void sendPacket(final @NotNull Packet packet);

    void sendPacketAndClose(final @NotNull Packet packet);

    void forceDisconnect();

    void setInformation(final @NotNull ClientInformation information);

    void setProfile(final @NotNull String name, final @NotNull UUID uuid);

    void setState(final @NotNull ConnectionState state);

    void setProtocol(final int protocol);

    void setHost(final @NotNull String hostname, final int hostPort);

    String getName();

    UUID getUUID();

    ClientInformation getInformation();

    @NotNull Channel getChannel();

    @NotNull ConnectionState getState();

    int getProtocol();

    @NotNull String getHostname();

    int getHostPort();

    boolean isFromTransfer();

}