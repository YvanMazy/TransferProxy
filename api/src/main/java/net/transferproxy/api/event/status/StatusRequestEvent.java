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

package net.transferproxy.api.event.status;

import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.status.StatusResponse;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StatusRequestEvent {

    private final PlayerConnection connection;
    private StatusResponse response;
    private boolean canSendResponsePacket;

    public StatusRequestEvent(final @NotNull PlayerConnection connection) {
        this(connection, true);
    }

    public StatusRequestEvent(final @NotNull PlayerConnection connection, final boolean canSendResponsePacket) {
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
        this.canSendResponsePacket = canSendResponsePacket;
    }

    @Contract(pure = true)
    public @NotNull PlayerConnection getConnection() {
        return this.connection;
    }

    @Contract(pure = true)
    public @Nullable StatusResponse getResponse() {
        return this.response;
    }

    public void setResponse(final @Nullable StatusResponse response) {
        this.response = response;
    }

    @Contract(pure = true)
    public boolean canSendResponsePacket() {
        return this.canSendResponsePacket;
    }

    public void setCanSendResponsePacket(final boolean canSendSuccessPacket) {
        this.canSendResponsePacket = canSendSuccessPacket;
    }

}