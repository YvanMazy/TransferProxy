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
    public Status getStatus() {
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
        public boolean useEpoll() {
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
        public boolean useTcpNoDelay() {
            return this.useTcpNoDelay;
        }

    }

    private static class YamlStatus implements Status {

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