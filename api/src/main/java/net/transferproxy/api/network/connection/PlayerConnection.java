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
import net.transferproxy.api.network.protocol.Protocolized;
import net.transferproxy.api.profile.ClientInformation;
import net.transferproxy.api.status.StatusResponse;
import net.transferproxy.api.util.CookieUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface PlayerConnection extends Protocolized {

    /**
     * Transfers the player to another server using the default Minecraft port (<strong>25565</strong>).
     *
     * @param host The target server hostname or IP address (must not be null)
     */
    default void transfer(final @NotNull String host) {
        this.transfer(host, 25565);
    }

    /**
     * Transfers the player to another server with a specified port.
     *
     * @param host The target server hostname or IP address (must not be null)
     * @param hostPort The target server port number
     */
    void transfer(final @NotNull String host, final int hostPort);

    /**
     * Sends a login success packet to authenticate the player.
     *
     * @param uuid The UUID to assign to the player (must not be null)
     * @param username The username to assign to the player (must not be null)
     */
    void sendLoginSuccess(final @NotNull UUID uuid, final @NotNull String username);

    /**
     * Sends a server status response to the client.
     *
     * @param response The status response object containing server information (must not be null)
     */
    void sendStatusResponse(final @NotNull StatusResponse response);

    /**
     * Fetches a client-stored cookie using a {@link Key} reference.
     *
     * @param cookieKey The cookie identifier as a Key object (must not be null)
     * @return A CompletableFuture that will contain the cookie's payload when available
     */
    default @NotNull CompletableFuture<byte[]> fetchCookie(final @NotNull Key cookieKey) {
        return this.fetchCookie(cookieKey.asString());
    }

    /**
     * Fetches a client-stored cookie using its string identifier.
     *
     * @param cookieKey The string identifier for the cookie (must not be null)
     * @return A CompletableFuture that will contain the cookie's payload when available
     */
    @NotNull CompletableFuture<byte[]> fetchCookie(final @NotNull String cookieKey);

    /**
     * Stores a cookie on the client using a {@link Key} reference.
     *
     * @param cookieKey The cookie identifier as a Key object (must not be null)
     * @param payload The binary data to store in the cookie (must not be null)
     * @throws IllegalArgumentException if the payload length is greater than {@link CookieUtil#getMaxCookieSize()}.
     */
    default void storeCookie(final @NotNull Key cookieKey, final byte @NotNull [] payload) {
        this.storeCookie(cookieKey.asString(), payload);
    }

    /**
     * Stores a cookie on the client using its string identifier.
     *
     * @param cookieKey The string identifier for the cookie (must not be null)
     * @param payload The binary data to store in the cookie (must not be null)
     * @throws IllegalArgumentException if the payload length is greater than {@link CookieUtil#getMaxCookieSize()}.
     */
    void storeCookie(final @NotNull String cookieKey, final byte @NotNull [] payload);

    /**
     * Handles a cookie response received from the client.
     *
     * @param cookieKey The cookie identifier as a Key object (must not be null)
     * @param payload The received cookie data, or null if no data was present
     */
    default void handleCookieResponse(final @NotNull Key cookieKey, final byte @Nullable [] payload) {
        this.handleCookieResponse(cookieKey.asString(), payload);
    }

    /**
     * Handles a cookie response received from the client.
     *
     * @param cookieKey The string identifier for the cookie (must not be null)
     * @param payload The received cookie data, or null if no data was present
     */
    void handleCookieResponse(final @NotNull String cookieKey, final byte @Nullable [] payload);

    /**
     * Get a view of all pending cookie requests waiting for client responses.
     *
     * @return An immutable map of cookie keys to their corresponding CompletableFuture objects
     */
    @UnmodifiableView
    @NotNull Map<String, CompletableFuture<byte[]>> getPendingCookies();

    /**
     * Removes all resource packs from the client.
     */
    default void removeResourcePacks() {
        this.removeResourcePack(null);
    }

    /**
     * Removes a specific resource pack identified by its UUID.
     *
     * @param uuid The UUID of the resource pack to remove, or null to remove all
     */
    void removeResourcePack(final @Nullable UUID uuid);

    /**
     * Resets the player's chat session state.
     */
    void resetChat();

    /**
     * Disconnects the player with a plain text reason.
     *
     * @param reason The disconnect message (must not be null)
     */
    default void disconnect(final @NotNull String reason) {
        this.disconnect(Component.text(reason));
    }

    /**
     * Disconnects the player with a formatted component reason.
     *
     * @param reason The disconnect message component (must not be null)
     */
    void disconnect(final @NotNull Component reason);

    /**
     * Sends a network packet to the player immediately.
     *
     * @param packet The packet to send (must not be null)
     */
    void sendPacket(final @NotNull Packet packet);

    /**
     * Sends a final packet to the player and close the connection.
     *
     * @param packet The last packet to send (must not be null)
     */
    void sendPacketAndClose(final @NotNull Packet packet);

    /**
     * Forces immediate disconnection without sending any packets.
     */
    void forceDisconnect();

    /**
     * Updates client configuration information.
     *
     * @param information The client settings and preferences (must not be null)
     */
    void setInformation(final @NotNull ClientInformation information);

    /**
     * Sets the player's game profile.
     *
     * @param name The player's username (must not be null)
     * @param uuid The player's unique identifier (must not be null)
     */
    void setProfile(final @NotNull String name, final @NotNull UUID uuid);

    /**
     * Changes the connection's protocol state.
     *
     * @param state The new connection state (must not be null)
     */
    void setState(final @NotNull ConnectionState state);

    /**
     * Sets the protocol version for this connection.
     *
     * @param protocol The protocol version number
     */
    void setProtocol(final int protocol);

    /**
     * Updates the connection's host information.
     *
     * @param hostname The server hostname (must not be null)
     * @param hostPort The server port number
     */
    void setHost(final @NotNull String hostname, final int hostPort);

    /**
     * Sets the client's self-reported brand identifier.
     *
     * @param brand The client brand string (e.g., "Vanilla"), or null to clear
     */
    void setBrand(final @Nullable String brand);

    /**
     * Configures the packet handler group for this connection.
     *
     * @param group The packet provider group to use (must not be null)
     */
    void setPacketProviderGroup(final @NotNull PacketProviderGroup group);

    /**
     * Gets the player's username.
     *
     * @return The player's current username
     */
    String getName();

    /**
     * Gets the player's unique identifier.
     *
     * @return The player's UUID
     */
    UUID getUUID();

    /**
     * Gets the current packet handler group.
     *
     * @return The active packet provider group
     */
    @NotNull PacketProviderGroup getPacketProviderGroup();

    /**
     * Gets the client's configuration information.
     *
     * @return The client information, or null if not available
     */
    @Nullable ClientInformation getInformation();

    /**
     * Gets the network channel for this connection.
     *
     * @return The underlying network channel
     */
    @NotNull Channel getChannel();

    /**
     * Gets the current protocol state of the connection.
     *
     * @return The active connection state
     */
    @NotNull ConnectionState getState();

    /**
     * Gets the client protocol version.
     *
     * @return The protocol version number
     */
    @Override
    int getProtocol();

    /**
     * Gets the connected server hostname.
     *
     * @return The current hostname
     */
    @NotNull String getHostname();

    /**
     * Gets the connected server port.
     *
     * @return The current port number
     */
    int getHostPort();

    /**
     * Checks if the client has reported a brand identifier.
     *
     * @return true if a brand is present, false otherwise
     */
    default boolean hasBrand() {
        return this.getBrand() != null;
    }

    /**
     * Gets the client's self-reported brand identifier.
     *
     * @return The client brand string, or null if not available
     */
    @Nullable String getBrand();

    /**
     * Determines if this connection was established through a server transfer.
     *
     * @return true if the connection resulted from a transfer, false otherwise
     */
    boolean isFromTransfer();

}