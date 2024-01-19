package be.darkkraft.transferproxy.api;

import be.darkkraft.transferproxy.api.configuration.ProxyConfiguration;
import be.darkkraft.transferproxy.api.module.ModuleManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class TransferProxy {

    private static TransferProxy instance;

    protected volatile boolean started;

    protected void setInstance(final @NotNull TransferProxy instance) {
        if (TransferProxy.instance != null) {
            throw new IllegalStateException("Instance is already defined");
        }
        TransferProxy.instance = Objects.requireNonNull(instance, "Instance cannot be null");
    }

    public abstract void start();

    public abstract void stop();

    public abstract @NotNull ProxyConfiguration getConfiguration();

    public abstract @NotNull ModuleManager getModuleManager();

    public abstract long getStartedTime();

    public boolean isStarted() {
        return this.started;
    }

    public static TransferProxy getInstance() {
        return instance;
    }

}