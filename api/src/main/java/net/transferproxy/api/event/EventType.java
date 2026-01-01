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

package net.transferproxy.api.event;

import net.transferproxy.api.event.listener.DefaultReadyListener;
import net.transferproxy.api.event.listener.EmptyListener;
import net.transferproxy.api.event.listener.EventListener;
import net.transferproxy.api.event.login.PreLoginEvent;
import net.transferproxy.api.event.status.StatusRequestEvent;
import net.transferproxy.api.network.connection.PlayerConnection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public enum EventType {

    HANDSHAKE(PlayerConnection.class, EmptyListener::getInstance),
    PRE_LOGIN(PreLoginEvent.class, EmptyListener::getInstance),
    STATUS(StatusRequestEvent.class, EmptyListener::getInstance),
    READY(PlayerConnection.class, DefaultReadyListener::new);

    private final Class<?> eventClass;
    private final Supplier<@NotNull EventListener<?>> defaultListener;

    EventType(final @NotNull Class<?> eventClass, final @NotNull Supplier<@NotNull EventListener<?>> defaultListener) {
        this.eventClass = Objects.requireNonNull(eventClass, "eventClass must not be null");
        this.defaultListener = Objects.requireNonNull(defaultListener, "defaultListener must not be null");
    }

    /**
     * Retrieves the class type of the event associated with this event type.
     *
     * @return the class of the event
     */
    @Contract(pure = true)
    public @NotNull Class<?> getEventClass() {
        return this.eventClass;
    }

    /**
     * Builds the default event listener for this event type.
     *
     * @return the default event listener
     */
    @Contract(pure = true)
    public @NotNull EventListener<?> buildDefaultListener() {
        return this.defaultListener.get();
    }

}