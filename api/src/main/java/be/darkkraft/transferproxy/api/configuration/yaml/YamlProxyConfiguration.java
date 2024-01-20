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

package be.darkkraft.transferproxy.api.configuration.yaml;

import be.darkkraft.transferproxy.api.configuration.ProxyConfiguration;
import io.netty.util.ResourceLeakDetector;

@SuppressWarnings("unused")
public class YamlProxyConfiguration implements ProxyConfiguration {

    private YamlNetwork network;
    private YamlStatus status;

    @Override
    public Network getNetwork() {
        return this.network;
    }

    @Override
    public ProxyConfiguration.Status getStatus() {
        return this.status;
    }

    private static class YamlNetwork implements ProxyConfiguration.Network {

        private final String bindAddress;
        private final int bindPort;
        private final ResourceLeakDetector.Level resourceLeakDetectorLevel;
        private final boolean useEpoll;
        private final int bossThreads;
        private final int workerThreads;
        private final boolean useTcpNoDelay;

        public YamlNetwork() {
            this.bindAddress = "localhost";
            this.bindPort = 25565;
            this.resourceLeakDetectorLevel = ResourceLeakDetector.Level.DISABLED;
            this.useEpoll = true;
            this.bossThreads = 1;
            this.workerThreads = 3;
            this.useTcpNoDelay = false;
        }

        @Override
        public String getBindAddress() {
            return this.bindAddress;
        }

        @Override
        public int getBindPort() {
            return this.bindPort;
        }

        @Override
        public ResourceLeakDetector.Level getResourceLeakDetectorLevel() {
            return this.resourceLeakDetectorLevel;
        }

        @Override
        public boolean isUseEpoll() {
            return this.useEpoll;
        }

        @Override
        public int getBossThreads() {
            return this.bossThreads;
        }

        @Override
        public int getWorkerThreads() {
            return this.workerThreads;
        }

        @Override
        public boolean isUseTcpNoDelay() {
            return this.useTcpNoDelay;
        }

    }

    private static class YamlStatus implements ProxyConfiguration.Status {

        private final String name;
        private final String description;
        private final String protocol;

        public YamlStatus() {
            this.name = "TransferProxy";
            this.description = "<green>A TransferProxy server";
            this.protocol = "AUTO";
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public String getProtocol() {
            return this.protocol;
        }

    }

}