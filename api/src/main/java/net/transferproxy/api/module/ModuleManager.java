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

package net.transferproxy.api.module;

import net.transferproxy.api.event.EventManager;
import net.transferproxy.api.network.packet.provider.PacketProviderGroup;
import net.transferproxy.api.plugin.PluginManager;
import net.transferproxy.api.terminal.TerminalExecutor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

/**
 * Manages the modules of the TransferProxy.
 */
public interface ModuleManager {

    /**
     * Initializes all undefined modules with default implementations.
     */
    void initializeDefaults();

    /**
     * Gets the current {@link EventManager} implementation.
     *
     * @return the event manager
     */
    @Contract(pure = true)
    @NotNull EventManager getEventManager();

    /**
     * Gets the current {@link PluginManager} implementation.
     *
     * @return the plugin manager
     */
    @Contract(pure = true)
    @NotNull PluginManager getPluginManager();

    /**
     * Gets the current {@link IntFunction<PacketProviderGroup>} implementation.
     * <p>This implementation will be used to create new packet provider groups from player protocol.</p>
     *
     * @return the packet provider group function
     */
    @Contract(pure = true)
    @NotNull IntFunction<PacketProviderGroup> getPacketProviderGroupFunction();

    /**
     * Gets the current {@link TerminalExecutor} implementation.
     * <p>This implementation will be used to execute terminal commands.</p>
     *
     * @return the terminal executor
     */
    @Contract(pure = true)
    @NotNull TerminalExecutor getTerminalExecutor();

    /**
     * Sets the {@link EventManager} implementation to use.
     *
     * @param eventManager the event manager
     */
    void setEventManager(final @NotNull EventManager eventManager);

    /**
     * Sets the {@link PluginManager} implementation to use.
     *
     * @param pluginManager the plugin manager
     */
    void setPluginManager(final @NotNull PluginManager pluginManager);

    /**
     * Sets the {@link IntFunction<PacketProviderGroup>} implementation to use.
     * <p>This implementation will be used to create new packet provider groups from player protocol.</p>
     *
     * @param packetProviderGroupFunction the packet provider group function
     */
    void setPacketProviderGroupFunction(final @NotNull IntFunction<PacketProviderGroup> packetProviderGroupFunction);

    /**
     * Sets the {@link TerminalExecutor} implementation to use.
     * <p>This implementation will be used to execute terminal commands.</p>
     *
     * @param terminalExecutor the terminal executor
     */
    void setTerminalExecutor(final @NotNull TerminalExecutor terminalExecutor);

}