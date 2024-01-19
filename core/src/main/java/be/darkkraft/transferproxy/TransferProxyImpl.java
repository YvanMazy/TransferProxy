package be.darkkraft.transferproxy;

import be.darkkraft.transferproxy.api.TransferProxy;
import be.darkkraft.transferproxy.api.configuration.ProxyConfiguration;
import be.darkkraft.transferproxy.api.module.ModuleManager;
import be.darkkraft.transferproxy.api.network.NetworkServer;
import be.darkkraft.transferproxy.module.ModuleManagerImpl;
import be.darkkraft.transferproxy.network.NettyNetworkServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TransferProxyImpl extends TransferProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransferProxyImpl.class);

    private final ProxyConfiguration configuration;
    private final ModuleManager moduleManager = new ModuleManagerImpl();
    private NetworkServer networkServer;

    private long startedTime;

    public TransferProxyImpl(final @NotNull ProxyConfiguration configuration) {
        this.setInstance(this);
        this.configuration = Objects.requireNonNull(configuration, "Configuration cannot be null");
    }

    @Override
    public void start() {
        this.started = true;
        LOGGER.info("Server is starting...");
        this.startedTime = System.currentTimeMillis();

        this.moduleManager.initializeDefaults();
        (this.networkServer = new NettyNetworkServer()).start();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "Proxy Shutdown Thread"));
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

        if (this.networkServer != null) {
            this.networkServer.stop();
        }

        LOGGER.info("Server is successfully stopped!");
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
    public long getStartedTime() {
        return this.startedTime;
    }

}