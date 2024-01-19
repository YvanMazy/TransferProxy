package be.darkkraft.transferproxy.module;

import be.darkkraft.transferproxy.api.login.EmptyLoginHandler;
import be.darkkraft.transferproxy.api.login.LoginHandler;
import be.darkkraft.transferproxy.api.module.ModuleManager;
import be.darkkraft.transferproxy.api.status.StatusHandler;
import be.darkkraft.transferproxy.status.DefaultStatusHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ModuleManagerImpl implements ModuleManager {

    private StatusHandler statusHandler;
    private LoginHandler loginHandler;

    @Override
    public void initializeDefaults() {
        if (this.statusHandler == null) {
            this.statusHandler = new DefaultStatusHandler();
        }
        if (this.loginHandler == null) {
            this.loginHandler = new EmptyLoginHandler();
        }
    }

    @Override
    public @NotNull StatusHandler getStatusHandler() {
        return this.statusHandler;
    }

    @Override
    public @NotNull LoginHandler getLoginHandler() {
        return this.loginHandler;
    }

    @Override
    public void setStatusHandler(final @NotNull StatusHandler statusHandler) {
        this.statusHandler = Objects.requireNonNull(statusHandler, "StatusHandler cannot be null");
    }

    @Override
    public void setLoginHandler(final @NotNull LoginHandler loginHandler) {
        this.loginHandler = Objects.requireNonNull(loginHandler, "LoginHandler cannot be null");
    }

}