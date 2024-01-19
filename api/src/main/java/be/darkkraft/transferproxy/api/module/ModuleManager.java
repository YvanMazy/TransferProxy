package be.darkkraft.transferproxy.api.module;

import be.darkkraft.transferproxy.api.login.LoginHandler;
import be.darkkraft.transferproxy.api.status.StatusHandler;
import org.jetbrains.annotations.NotNull;

public interface ModuleManager {

    void initializeDefaults();

    @NotNull StatusHandler getStatusHandler();

    @NotNull LoginHandler getLoginHandler();

    void setStatusHandler(final @NotNull StatusHandler statusHandler);

    void setLoginHandler(final @NotNull LoginHandler loginHandler);

}