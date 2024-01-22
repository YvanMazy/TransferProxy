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

package be.darkkraft.transferproxy.api.module;

import be.darkkraft.transferproxy.api.event.EventType;
import be.darkkraft.transferproxy.api.event.listener.EventListener;
import be.darkkraft.transferproxy.api.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public interface ModuleManager {

    void initializeDefaults();

    void call(final @NotNull EventType eventType, final @NotNull Object event);

    @NotNull <T extends EventListener<?>> T getListener(final @NotNull EventType eventType);

    @NotNull PluginManager getPluginManager();

    <T extends EventListener<?>> void setListener(final @NotNull EventType eventType, final @NotNull T eventListener);

    void setPluginManager(final @NotNull PluginManager pluginManager);

}