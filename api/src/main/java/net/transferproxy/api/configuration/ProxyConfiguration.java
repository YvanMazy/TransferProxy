/*
 * MIT License
 *
 * Copyright (c) 2024 Yvan Mazy
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

package net.transferproxy.api.configuration;

import io.netty.util.ResourceLeakDetector;
import net.transferproxy.api.TransferProxy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the configuration of {@link TransferProxy}
 */
public interface ProxyConfiguration {

    /**
     * Retrieves the {@link Network} configuration.
     *
     * @return the network configuration, never {@code null}
     */
    @NotNull
    @Contract(pure = true)
    Network getNetwork();

    /**
     * Retrieves the {@link Status} configuration.
     *
     * @return the status configuration, never {@code null}
     */
    @NotNull
    @Contract(pure = true)
    Status getStatus();

    /**
     * Retrieves the {@link Miscellaneous} configuration.
     *
     * @return the miscellaneous configuration, never {@code null}
     */
    @NotNull
    @Contract(pure = true)
    Miscellaneous getMiscellaneous();

    /**
     * Retrieves the {@link Logging} configuration.
     *
     * @return the logging configuration, never {@code null}
     */
    @NotNull
    @Contract(pure = true)
    Logging getLogging();

    /**
     * Configuration for network-related settings, including server binding and performance tuning.
     */
    interface Network {

        /**
         * Returns the IP address to which the server is bound.
         *
         * @return the bind IP address as a string, never {@code null}
         */
        @NotNull
        @Contract(pure = true)
        String getBindAddress();

        /**
         * Gets the port number on which the server listens for incoming connections.
         *
         * @return the bind port number
         */
        @Contract(pure = true)
        int getBindPort();

        /**
         * Gets the resource leak detection level for Netty's allocator.
         * Higher levels provide more detailed tracking but may impact performance.
         * Recommended to be set to 'DISABLED' in production environments.
         *
         * @return the configured leak detection level, never {@code null}
         */
        @NotNull
        @Contract(pure = true)
        ResourceLeakDetector.Level getResourceLeakDetectorLevel();

        /**
         * Determines if the Epoll transport is enabled for enhanced performance on Linux systems.
         * This should be enabled when the server is running on a Linux-based OS.
         *
         * @return {@code true} if Epoll is enabled, {@code false} otherwise
         */
        @Contract(pure = true)
        boolean isUseEpoll();

        /**
         * Gets the number of threads dedicated to accepting incoming connections.
         * Typically configured to 1 unless handling extremely high connection rates.
         *
         * @return the number of boss threads
         */
        @Contract(pure = true)
        int getBossThreads();

        /**
         * Gets the number of threads used for processing network I/O operations.
         * Adjust based on workload and available CPU cores.
         *
         * @return the number of worker threads
         */
        @Contract(pure = true)
        int getWorkerThreads();

        /**
         * Indicates whether Nagle's algorithm is disabled.
         * When {@code true}, reduces latency by sending packets immediately at the cost of increased network traffic.
         *
         * @return {@code true} if TCP_NO_DELAY is enabled, {@code false} otherwise
         */
        @Contract(pure = true)
        boolean isUseTcpNoDelay();

        /**
         * Determines if the server performs extra byte validation on incoming packets.
         *
         * @return {@code true} if extra byte checks are disabled, {@code false} otherwise
         */
        @Contract(pure = true)
        boolean isDisableExtraByteCheck();

    }

    /**
     * Configuration for server status presentation in client server lists.
     */
    interface Status {

        /**
         * Gets the server name displayed in client server lists.
         *
         * @return the server name, never {@code null}
         */
        @NotNull
        @Contract(pure = true)
        String getName();

        /**
         * Gets the server description using MiniMessage formatting.
         *
         * @return the server description, never {@code null}
         */
        @NotNull
        @Contract(pure = true)
        String getDescription();

        /**
         * Gets the protocol version of proxy sent to clients.
         * 'AUTO' automatically matches the client's version.
         *
         * @return the protocol version identifier, never {@code null}
         */
        @NotNull
        @Contract(pure = true)
        String getProtocol();

        /**
         * Gets the filesystem path to the server's favicon image.
         * If the file is unavailable, no favicon is displayed in server lists.
         *
         * @return the favicon file path, never {@code null}
         */
        @NotNull
        @Contract(pure = true)
        String getFaviconPath();

    }

    /**
     * Configuration for miscellaneous server behavior and connection handling.
     */
    interface Miscellaneous {

        /**
         * Determines if clients using outdated protocol versions are automatically disconnected.
         *
         * @return {@code true} if outdated clients are kicked, {@code false} otherwise
         */
        @Contract(pure = true)
        boolean isKickOldProtocol();

        /**
         * Gets the message sent to clients disconnected for using outdated protocols.
         * Supports MiniMessage formatting for styled text.
         *
         * @return the kick message, never {@code null}
         */
        @NotNull
        @Contract(pure = true)
        String getKickOldProtocolMessage();

        /**
         * Determines if keep-alive packets are sent to clients in the CONFIG state to maintain
         * active connections during prolonged operations.
         *
         * @return {@code true} if keep-alive is enabled, {@code false} otherwise
         */
        @Contract(pure = true)
        boolean isKeepAlive();

        /**
         * Gets the interval (in milliseconds) between keep-alive packets sent to clients.
         * Effective only if keep-alive is enabled.
         *
         * @return the keep-alive packet interval
         */
        @Contract(pure = true)
        long getKeepAliveDelay();

    }

    /**
     * Configuration for logging events and errors.
     */
    interface Logging {

        /**
         * Determines if connection establishment events are logged.
         *
         * @return {@code true} if connect events are logged, {@code false} otherwise
         */
        @Contract(pure = true)
        boolean isLogConnect();

        /**
         * Determines if normal connection disconnection events are logged.
         *
         * @return {@code true} if disconnect events are logged, {@code false} otherwise
         */
        @Contract(pure = true)
        boolean isLogDisconnect();

        /**
         * Determines if connection timeout events are logged.
         *
         * @return {@code true} if timeout events are logged, {@code false} otherwise
         */
        @Contract(pure = true)
        boolean isLogTimeout();

        /**
         * Determines if disconnections caused by exceptions are logged.
         *
         * @return {@code true} if exception disconnections are logged, {@code false} otherwise
         */
        @Contract(pure = true)
        boolean isLogDisconnectForException();

        /**
         * Determines if connection transfer events (e.g., moving to another server) are logged.
         *
         * @return {@code true} if transfer events are logged, {@code false} otherwise
         */
        @Contract(pure = true)
        boolean isLogTransfer();

        /**
         * Determines if full exception stack traces are logged for exception-based disconnections.
         * Only effective if {@link #isLogDisconnectForException()} is enabled.
         *
         * @return {@code true} if full exceptions are logged, {@code false} otherwise
         */
        @Contract(pure = true)
        boolean isLogCompleteDisconnectException();

    }

}