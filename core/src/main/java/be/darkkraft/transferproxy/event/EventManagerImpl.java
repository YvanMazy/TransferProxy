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

package be.darkkraft.transferproxy.event;

import be.darkkraft.transferproxy.api.event.EventManager;
import be.darkkraft.transferproxy.api.event.EventType;
import be.darkkraft.transferproxy.api.event.listener.DefaultReadyListener;
import be.darkkraft.transferproxy.api.event.listener.EventListener;
import be.darkkraft.transferproxy.api.status.listener.DefaultStatusListener;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class EventManagerImpl implements EventManager {

    private final Map<EventType, EventListener<?>[]> listenerMap = new EnumMap<>(EventType.class);

    public EventManagerImpl() {
        this.addListener(EventType.READY, new DefaultReadyListener());
        this.addListener(EventType.STATUS, new DefaultStatusListener());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void call(final @NotNull EventType eventType, final @NotNull Object event) {
        Objects.requireNonNull(eventType, "eventType cannot be null");
        Objects.requireNonNull(event, "event cannot be null");
        if (!eventType.getEventClass().isInstance(event)) {
            throw new IllegalArgumentException("Invalid event type for event: " + event.getClass() + "/" + eventType.getEventClass());
        }
        final EventListener<?>[] listeners = this.listenerMap.get(eventType);
        if (listeners != null) {
            for (final EventListener listener : listeners) {
                listener.handle(event);
            }
        }
    }

    @Override
    public synchronized <T extends EventListener<?>> void addListener(final @NotNull EventType eventType, final @NotNull T eventListener) {
        Objects.requireNonNull(eventType, "eventType cannot be null");
        Objects.requireNonNull(eventListener, "eventListener cannot be null");
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
    public synchronized <T extends EventListener<?>> boolean removeListener(final @NotNull EventType eventType, @NotNull final T eventListener) {
        Objects.requireNonNull(eventType, "eventType cannot be null");
        Objects.requireNonNull(eventListener, "eventListener cannot be null");
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

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T extends EventListener<?>> T[] getListeners(final @NotNull EventType eventType) {
        return (T[]) this.listenerMap.get(eventType);
    }

}