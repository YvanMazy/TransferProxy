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

package net.transferproxy;

import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.configuration.ProxyConfiguration;
import net.transferproxy.api.module.ModuleManager;
import net.transferproxy.api.network.NetworkServer;
import net.transferproxy.keepalive.KeepAliveTask;
import net.transferproxy.module.ModuleManagerImpl;
import net.transferproxy.network.NettyNetworkServer;
import net.transferproxy.terminal.TerminalThread;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinylog.provider.ProviderRegistry;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TransferProxyImpl extends TransferProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransferProxyImpl.class);

    private final ProxyConfiguration configuration;
    private final ModuleManager moduleManager = new ModuleManagerImpl();
    private NetworkServer networkServer;
    private ScheduledExecutorService keepAliveExecutor;

    private long startedTime;

    public TransferProxyImpl(final @NotNull ProxyConfiguration configuration) {
        this.setInstance(this);
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
    }

    @Override
    public void start() {
        this.started = true;
        LOGGER.info("Server is starting...");
        this.startedTime = System.currentTimeMillis();

        this.moduleManager.initializeDefaults();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "Shutdown Thread"));

        this.moduleManager.getPluginManager().start();
        (this.networkServer = new NettyNetworkServer()).start();

        if (this.configuration.getMiscellaneous().isKeepAlive()) {
            this.keepAliveExecutor = Executors.newSingleThreadScheduledExecutor();
            final long delay = this.configuration.getMiscellaneous().getKeepAliveDelay();
            this.keepAliveExecutor.scheduleAtFixedRate(new KeepAliveTask(), delay, delay, TimeUnit.MILLISECONDS);
        }

        new TerminalThread(this::isStarted, this.moduleManager::getTerminalExecutor).start();

        LOGGER.info("Server started successfully in {}ms", System.currentTimeMillis() - this.startedTime);
    }

    @Override
    public void stop() {
        if (!this.isStarted()) {
            LOGGER.warn("Server cannot be stopped because it is not started!");
            return;
        }
        this.started = false;
        LOGGER.info("Server is shutting down...");

        if (this.keepAliveExecutor != null) {
            this.keepAliveExecutor.shutdownNow();
        }

        this.moduleManager.getPluginManager().stop();

        if (this.networkServer != null) {
            this.networkServer.stop();
        }

        LOGGER.info("Server is successfully stopped!");
        try {
            ProviderRegistry.getLoggingProvider().shutdown();
        } catch (final InterruptedException ignored) {
        }
    }

    @Override
    public @NotNull ProxyConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public @NotNull ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    @Override
    public NetworkServer getNetworkServer() {
        return this.networkServer;
    }

    @Override
    public long getStartedTime() {
        return this.startedTime;
    }

}