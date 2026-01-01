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

package net.transferproxy.event;

import net.transferproxy.api.event.EventManager;
import net.transferproxy.api.event.EventType;
import net.transferproxy.api.event.listener.EventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public final class EventManagerImpl implements EventManager {

    private final Map<EventType, EventListener<?>[]> listenerMap = new EnumMap<>(EventType.class);
    private final Map<EventType, EventListener<?>> defaultListenerMap = new EnumMap<>(EventType.class);

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void call(final @NotNull EventType eventType, final @NotNull Object event) {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(event, "event must not be null");
        if (!eventType.getEventClass().isInstance(event)) {
            throw new IllegalArgumentException("Invalid event type for event: " + event.getClass() + "/" + eventType.getEventClass());
        }
        final EventListener<?>[] listeners = this.listenerMap.get(eventType);
        if (listeners != null) {
            for (final EventListener listener : listeners) {
                listener.handle(event);
            }
            return;
        }
        ((EventListener) this.defaultListenerMap.computeIfAbsent(eventType, EventType::buildDefaultListener)).handle(event);
    }

    @Override
    public synchronized <T extends EventListener<?>> void addListener(final @NotNull EventType eventType, final @NotNull T eventListener) {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(eventListener, "eventListener must not be null");
        this.listenerMap.compute(eventType, (key, listeners) -> {
            if (listeners == null) {
                return new EventListener<?>[] {eventListener};
            }
            final EventListener<?>[] array = new EventListener[listeners.length + 1];
            System.arraycopy(listeners, 0, array, 0, listeners.length);
            array[listeners.length] = eventListener;
            return array;
        });
    }

    @Override
    public synchronized <T extends EventListener<?>> boolean removeListener(final EventType eventType, final T eventListener) {
        if (eventType == null || eventListener == null) {
            return false;
        }
        final EventListener<?>[] listeners = this.listenerMap.get(eventType);
        if (listeners == null) {
            return false;
        }
        final int length = listeners.length;
        if (length == 1) {
            if (listeners[0].equals(eventListener)) {
                this.listenerMap.remove(eventType);
                return true;
            }
            return false;
        }

        boolean found = false;
        int index = -1;
        for (int i = 0; i < length; i++) {
            if (listeners[i].equals(eventListener)) {
                found = true;
                index = i;
                break;
            }
        }

        if (!found) {
            return false;
        }

        final EventListener<?>[] array = new EventListener<?>[length - 1];
        System.arraycopy(listeners, 0, array, 0, index);
        System.arraycopy(listeners, index + 1, array, index, length - index - 1);

        this.listenerMap.put(eventType, array);
        return true;
    }

    @Override
    public @Unmodifiable @NotNull Collection<? extends EventListener<?>> getListeners(final @NotNull EventType eventType) {
        Objects.requireNonNull(eventType, "eventType must not be null");
        final EventListener<?>[] listeners = this.listenerMap.get(eventType);
        if (listeners != null) {
            return List.of(listeners);
        }
        return List.of();
    }

}