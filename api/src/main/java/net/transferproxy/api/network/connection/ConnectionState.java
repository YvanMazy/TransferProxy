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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum ConnectionState {

    HANDSHAKE,
    STATUS,
    LOGIN(true),
    TRANSFER(true),
    CONFIG(true),
    CLOSED;

    private final boolean login;

    ConnectionState() {
        this(false);
    }

    ConnectionState(final boolean login) {
        this.login = login;
    }

    /**
     * Checks if the connection is in a login state.
     *
     * @return If the connection is in a login state.
     */
    @Contract(pure = true)
    public boolean isLogin() {
        return this.login;
    }

    /**
     * Gets the connection state from the id.
     *
     * @param id The id of the connection state.
     * @return The connection state.
     * @throws IllegalArgumentException If the id is invalid.
     */
    public static @NotNull ConnectionState fromId(final int id) {
        return switch (id) {
            case 1 -> STATUS;
            case 2 -> LOGIN;
            case 3 -> TRANSFER;
            default -> throw new IllegalArgumentException("Invalid ConnectionState id: " + id);
        };
    }

}