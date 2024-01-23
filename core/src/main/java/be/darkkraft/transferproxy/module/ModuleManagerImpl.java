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

package be.darkkraft.transferproxy.module;

import be.darkkraft.transferproxy.api.event.EventType;
import be.darkkraft.transferproxy.api.event.listener.EventListener;
import be.darkkraft.transferproxy.api.module.ModuleManager;
import be.darkkraft.transferproxy.api.plugin.PluginManager;
import be.darkkraft.transferproxy.plugin.PluginManagerImpl;
import be.darkkraft.transferproxy.api.status.listener.DefaultStatusListener;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class ModuleManagerImpl implements ModuleManager {

    private final Map<EventType, EventListener<?>> listenerMap = new EnumMap<>(EventType.class);
    private PluginManager pluginManager;

    @Override
    public void initializeDefaults() {
        this.listenerMap.computeIfAbsent(EventType.STATUS, ignored -> new DefaultStatusListener());
        if (this.pluginManager == null) {
            this.pluginManager = new PluginManagerImpl();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void call(final @NotNull EventType eventType, final @NotNull Object event) {
        Objects.requireNonNull(eventType, "eventType cannot be null");
        Objects.requireNonNull(event, "event cannot be null");
        if (!eventType.getEventClass().isInstance(event)) {
            throw new IllegalArgumentException("Invalid event type for event: " + event.getClass() + "/" + eventType.getEventClass());
        }
        final EventListener listener = this.listenerMap.get(eventType);
        if (listener != null) {
            listener.handle(event);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T extends EventListener<?>> T getListener(final @NotNull EventType eventType) {
        return (T) this.listenerMap.get(eventType);
    }

    @Override
    public @NotNull PluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Override
    public <T extends EventListener<?>> void setListener(final @NotNull EventType eventType, final @NotNull T eventListener) {
        Objects.requireNonNull(eventType, "eventType cannot be null");
        Objects.requireNonNull(eventListener, "eventListener cannot be null");
        this.listenerMap.put(eventType, eventListener);
    }

    @Override
    public void setPluginManager(final @NotNull PluginManager pluginManager) {
        this.pluginManager = Objects.requireNonNull(pluginManager, "pluginManager cannot be null");
    }

}