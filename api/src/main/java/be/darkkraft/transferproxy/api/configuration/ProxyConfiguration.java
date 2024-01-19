package be.darkkraft.transferproxy.api.configuration;

import io.netty.util.ResourceLeakDetector;

public interface ProxyConfiguration {

    Network getNetwork();

    Status getStatus();

    interface Network {

        String getBindAddress();

        int getBindPort();

        ResourceLeakDetector.Level getResourceLeakDetectorLevel();

        boolean useEpoll();

        int getBossThreads();

        int getWorkerThreads();

        boolean useTcpNoDelay();

    }

    interface Status {

        String getName();

        String getDescription();

        String getProtocol();

    }

}