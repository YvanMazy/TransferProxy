/*
 * MIT License
 *
 * Copyright (c) 2026 Yvan Mazy
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

package net.transferproxy.api;

import net.transferproxy.api.configuration.ProxyConfiguration;
import net.transferproxy.api.module.ModuleManager;
import net.transferproxy.api.network.NetworkServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class TransferProxy {

    private static TransferProxy instance;

    protected volatile boolean started;

    protected void setInstance(final @NotNull TransferProxy instance) {
        if (TransferProxy.instance != null) {
            throw new IllegalStateException("Instance is already defined");
        }
        TransferProxy.instance = Objects.requireNonNull(instance, "Instance must not be null");
    }

    public abstract void start();

    public abstract void stop();

    public abstract @NotNull ProxyConfiguration getConfiguration();

    public abstract @NotNull ModuleManager getModuleManager();

    public abstract NetworkServer getNetworkServer();

    public abstract long getStartedTime();

    public boolean isStarted() {
        return this.started;
    }

    public static TransferProxy getInstance() {
        return instance;
    }

}