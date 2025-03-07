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

package net.transferproxy.module;

import net.transferproxy.api.event.EventManager;
import net.transferproxy.api.module.ModuleManager;
import net.transferproxy.api.network.packet.provider.PacketProviderGroup;
import net.transferproxy.api.plugin.PluginManager;
import net.transferproxy.api.terminal.TerminalExecutor;
import net.transferproxy.api.terminal.DefaultTerminalExecutor;
import net.transferproxy.event.EventManagerImpl;
import net.transferproxy.network.packet.provider.PacketProviderGroups;
import net.transferproxy.plugin.PluginManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Objects;
import java.util.function.IntFunction;

public class ModuleManagerImpl implements ModuleManager {

    private EventManager eventManager;
    private PluginManager pluginManager;
    private IntFunction<PacketProviderGroup> packetProviderGroupFunction;
    private TerminalExecutor terminalExecutor;

    @Override
    public void initializeDefaults() {
        this.initializeDefaults(false);
    }

    @Override
    public @NotNull EventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public @NotNull PluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Override
    public @NotNull IntFunction<PacketProviderGroup> getPacketProviderGroupFunction() {
        return this.packetProviderGroupFunction;
    }

    @Override
    public @NotNull TerminalExecutor getTerminalExecutor() {
        return this.terminalExecutor;
    }

    @Override
    public void setPluginManager(final @NotNull PluginManager pluginManager) {
        this.pluginManager = Objects.requireNonNull(pluginManager, "pluginManager must not be null");
    }

    @Override
    public void setEventManager(final @NotNull EventManager eventManager) {
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager must not be null");
    }

    @Override
    public void setPacketProviderGroupFunction(final @NotNull IntFunction<PacketProviderGroup> packetProviderGroupFunction) {
        this.packetProviderGroupFunction =
                Objects.requireNonNull(packetProviderGroupFunction, "packetProviderGroupFunction must not be null");
    }

    @Override
    public void setTerminalExecutor(final @NotNull TerminalExecutor terminalExecutor) {
        this.terminalExecutor = Objects.requireNonNull(terminalExecutor, "terminalExecutor must not be null");
    }

    @VisibleForTesting
    public void initializeDefaults(final boolean force) {
        if (force || this.eventManager == null) {
            this.eventManager = new EventManagerImpl();
        }
        if (force || this.pluginManager == null) {
            this.pluginManager = new PluginManagerImpl();
        }
        if (force || this.packetProviderGroupFunction == null) {
            this.packetProviderGroupFunction = PacketProviderGroups::determineGroup;
        }
        if (force || this.terminalExecutor == null) {
            this.terminalExecutor = new DefaultTerminalExecutor();
        }
    }

}