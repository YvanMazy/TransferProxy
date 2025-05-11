/*
 * MIT License
 *
 * Copyright (c) 2025 Yvan Mazy
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

package net.transferproxy.api.util.test;

import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.configuration.ProxyConfiguration;
import net.transferproxy.api.configuration.yaml.YamlProxyConfiguration;
import net.transferproxy.api.module.ModuleManager;
import net.transferproxy.api.network.NetworkServer;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

public class MockedTransferProxy extends TransferProxy {

    public static void mock() {
        new MockedTransferProxy().start();
    }

    // I use the yaml configuration because it has the default values
    private final ProxyConfiguration configuration = new YamlProxyConfiguration();
    private final ModuleManager moduleManager = Mockito.mock(ModuleManager.class);
    private final NetworkServer networkServer = Mockito.mock(NetworkServer.class);
    private final long startedTime = System.currentTimeMillis();

    @Override
    public void start() {
        this.setInstance(this);
    }

    @Override
    public void stop() {

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