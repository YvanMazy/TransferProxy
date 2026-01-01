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

package net.transferproxy.api.configuration.yaml;

import io.netty.util.ResourceLeakDetector;
import net.transferproxy.api.configuration.ProxyConfiguration;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class YamlProxyConfiguration implements ProxyConfiguration {

    private YamlNetwork network;
    private YamlStatus status;
    private YamlMiscellaneous miscellaneous;
    private YamlLogging logging;

    @Override
    public ProxyConfiguration.@NotNull Network getNetwork() {
        return this.network != null ? this.network : (this.network = new YamlNetwork());
    }

    @Override
    public ProxyConfiguration.@NotNull Status getStatus() {
        return this.status != null ? this.status : (this.status = new YamlStatus());
    }

    @Override
    public ProxyConfiguration.@NotNull Miscellaneous getMiscellaneous() {
        return this.miscellaneous != null ? this.miscellaneous : (this.miscellaneous = new YamlMiscellaneous());
    }

    @Override
    public ProxyConfiguration.@NotNull Logging getLogging() {
        return this.logging != null ? this.logging : (this.logging = new YamlLogging());
    }

    private static class YamlNetwork implements ProxyConfiguration.Network {

        private final String bindAddress;
        private final int bindPort;
        private final ResourceLeakDetector.Level resourceLeakDetectorLevel;
        private final boolean useEpoll;
        private final int bossThreads;
        private final int workerThreads;
        private final boolean useTcpNoDelay;
        private final boolean disableExtraByteCheck;

        private YamlNetwork() {
            this.bindAddress = "localhost";
            this.bindPort = 25565;
            this.resourceLeakDetectorLevel = ResourceLeakDetector.Level.DISABLED;
            this.useEpoll = true;
            this.bossThreads = 1;
            this.workerThreads = 3;
            this.useTcpNoDelay = false;
            this.disableExtraByteCheck = false;
        }

        @Override
        public @NotNull String getBindAddress() {
            return this.bindAddress;
        }

        @Override
        public int getBindPort() {
            return this.bindPort;
        }

        @Override
        public ResourceLeakDetector.@NotNull Level getResourceLeakDetectorLevel() {
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

        @Override
        public boolean isDisableExtraByteCheck() {
            return this.disableExtraByteCheck;
        }

    }

    private static class YamlStatus implements ProxyConfiguration.Status {

        private final String name;
        private final String description;
        private final String protocol;
        private final int online;
        private final int maxOnline;
        private final String faviconPath;

        private YamlStatus() {
            this.name = "TransferProxy";
            this.description = "<green>A TransferProxy server";
            this.protocol = "AUTO";
            this.online = -1;
            this.maxOnline = -1;
            this.faviconPath = "./favicon.png";
        }

        @Override
        public @NotNull String getName() {
            return this.name;
        }

        @Override
        public @NotNull String getDescription() {
            return this.description;
        }

        @Override
        public @NotNull String getProtocol() {
            return this.protocol;
        }

        @Override
        public @NotNull String getFaviconPath() {
            return this.faviconPath;
        }

        @Override
        public int getOnline() {
            return this.online;
        }

        @Override
        public int getMaxOnline() {
            return this.maxOnline;
        }

    }

    private static class YamlMiscellaneous implements ProxyConfiguration.Miscellaneous {

        private final boolean kickOldProtocol;
        private final String kickOldProtocolMessage;
        private final boolean keepAlive;
        private final long keepAliveDelay;

        private YamlMiscellaneous() {
            this.kickOldProtocol = true;
            this.kickOldProtocolMessage = "<red>Outdated client";
            this.keepAlive = false;
            this.keepAliveDelay = 5_000L;
        }

        @Override
        public boolean isKickOldProtocol() {
            return this.kickOldProtocol;
        }

        @Override
        public @NotNull String getKickOldProtocolMessage() {
            return this.kickOldProtocolMessage;
        }

        @Override
        public boolean isKeepAlive() {
            return this.keepAlive;
        }

        @Override
        public long getKeepAliveDelay() {
            return this.keepAliveDelay;
        }

    }

    private static class YamlLogging implements ProxyConfiguration.Logging {

        private final boolean logConnect;
        private final boolean logDisconnect;
        private final boolean logTimeout;
        private final boolean logDisconnectForException;
        private final boolean logTransfer;
        private final boolean logCompleteDisconnectException;

        private YamlLogging() {
            this.logConnect = true;
            this.logDisconnect = true;
            this.logTimeout = true;
            this.logDisconnectForException = true;
            this.logTransfer = true;
            this.logCompleteDisconnectException = false;
        }

        @Override
        public boolean isLogConnect() {
            return this.logConnect;
        }

        @Override
        public boolean isLogDisconnect() {
            return this.logDisconnect;
        }

        @Override
        public boolean isLogTimeout() {
            return this.logTimeout;
        }

        @Override
        public boolean isLogDisconnectForException() {
            return this.logDisconnectForException;
        }

        @Override
        public boolean isLogTransfer() {
            return this.logTransfer;
        }

        @Override
        public boolean isLogCompleteDisconnectException() {
            return this.logCompleteDisconnectException;
        }

    }

}