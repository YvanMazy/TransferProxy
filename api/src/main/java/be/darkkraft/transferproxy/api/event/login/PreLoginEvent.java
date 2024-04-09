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

package be.darkkraft.transferproxy.api.event.login;

import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class PreLoginEvent {

    private final PlayerConnection connection;
    private UUID uuid;
    private String username;
    private boolean canSendSuccessPacket;

    public PreLoginEvent(final @NotNull PlayerConnection connection, final @NotNull UUID uuid, final @NotNull String username) {
        this(connection, uuid, username, true);
    }

    public PreLoginEvent(final @NotNull PlayerConnection connection,
                         final @NotNull UUID uuid,
                         final @NotNull String username,
                         final boolean canSendSuccessPacket) {
        this.connection = connection;
        this.uuid = uuid;
        this.username = username;
        this.canSendSuccessPacket = canSendSuccessPacket;
    }

    public PlayerConnection getConnection() {
        return this.connection;
    }

    public @NotNull UUID getUUID() {
        return this.uuid;
    }

    public @NotNull String getUsername() {
        return this.username;
    }

    public boolean canSendSuccessPacket() {
        return this.canSendSuccessPacket;
    }

    public void setUUID(final @NotNull UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid, "uuid must not be null");
    }

    public void setUsername(final String username) {
        this.username = Objects.requireNonNull(username, "username must not be null");
    }

    public void setCanSendSuccessPacket(final boolean canSendSuccessPacket) {
        this.canSendSuccessPacket = canSendSuccessPacket;
    }

}