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

package net.transferproxy.api.network.connection;

import io.netty.channel.Channel;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.api.network.packet.provider.PacketProviderGroup;
import net.transferproxy.api.profile.ClientInformation;
import net.transferproxy.api.status.StatusResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerConnection {

    default void transfer(final @NotNull String host) {
        this.transfer(host, 25565);
    }

    void transfer(final @NotNull String host, final int hostPort);

    void sendLoginSuccess(final @NotNull UUID uuid, final @NotNull String username);

    void sendStatusResponse(final @NotNull StatusResponse response);

    default @NotNull CompletableFuture<byte[]> fetchCookie(final @NotNull Key cookieKey) {
        return this.fetchCookie(cookieKey.asString());
    }

    @NotNull CompletableFuture<byte[]> fetchCookie(final @NotNull String cookieKey);

    default void storeCookie(final @NotNull Key cookieKey, final byte @NotNull [] payload) {
        this.storeCookie(cookieKey.asString(), payload);
    }

    void storeCookie(final @NotNull String cookieKey, final byte @NotNull [] payload);

    default void handleCookieResponse(final @NotNull Key cookieKey, final byte @Nullable [] payload) {
        this.handleCookieResponse(cookieKey.asString(), payload);
    }

    void handleCookieResponse(final @NotNull String cookieKey, final byte @Nullable [] payload);

    @NotNull Map<String, CompletableFuture<byte[]>> getPendingCookies();

    default void removeResourcePacks() {
        this.removeResourcePack(null);
    }

    void removeResourcePack(final @Nullable UUID uuid);

    void resetChat();

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

    void setBrand(final @Nullable String brand);

    void setPacketProviderGroup(final @NotNull PacketProviderGroup group);

    String getName();

    UUID getUUID();

    @NotNull PacketProviderGroup getPacketProviderGroup();

    @Nullable ClientInformation getInformation();

    @NotNull Channel getChannel();

    @NotNull ConnectionState getState();

    int getProtocol();

    @NotNull String getHostname();

    int getHostPort();

    default boolean hasBrand() {
        return this.getBrand() != null;
    }

    @Nullable String getBrand();

    boolean isFromTransfer();

}