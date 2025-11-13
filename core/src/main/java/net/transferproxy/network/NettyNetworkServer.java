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

package net.transferproxy.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GlobalEventExecutor;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.configuration.ProxyConfiguration;
import net.transferproxy.api.network.NetworkServer;
import net.transferproxy.network.connection.PlayerConnectionImpl;
import net.transferproxy.network.frame.clientbound.PacketEncoder;
import net.transferproxy.network.frame.clientbound.VarIntFrameEncoder;
import net.transferproxy.network.frame.serverbound.PacketDecoder;
import net.transferproxy.network.frame.serverbound.VarIntFrameDecoder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class NettyNetworkServer extends ChannelInitializer<Channel> implements NetworkServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyNetworkServer.class);
    private static final ChannelHandler FRAME_ENCODER = new VarIntFrameEncoder();

    private final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    private boolean checkExtraByte;

    @Override
    public void start() {
        final ProxyConfiguration.Network config = TransferProxy.getInstance().getConfiguration().getNetwork();
        ResourceLeakDetector.setLevel(config.getResourceLeakDetectorLevel());

        final Class<? extends ServerChannel> channelClass;

        final DefaultThreadFactory bossFactory = new DefaultThreadFactory("Boss Thread");
        final DefaultThreadFactory workerFactory = new DefaultThreadFactory("Worker Thread");

        final int bossThread = config.getBossThreads();
        final int workerThread = config.getWorkerThreads();

        // Use Epoll if available
        if (config.isUseEpoll() && Epoll.isAvailable()) {
            this.bossGroup = new MultiThreadIoEventLoopGroup(bossThread, bossFactory, EpollIoHandler.newFactory());
            this.workerGroup = new MultiThreadIoEventLoopGroup(workerThread, workerFactory, EpollIoHandler.newFactory());
            channelClass = EpollServerSocketChannel.class;
            LOGGER.info("The network will use the EPOLL channel type");
        } else {
            this.bossGroup = new MultiThreadIoEventLoopGroup(bossThread, bossFactory, NioIoHandler.newFactory());
            this.workerGroup = new MultiThreadIoEventLoopGroup(workerThread, workerFactory, NioIoHandler.newFactory());
            channelClass = NioServerSocketChannel.class;
            LOGGER.info("The network will use the NIO channel type");
        }

        final InetSocketAddress address = new InetSocketAddress(config.getBindAddress(), config.getBindPort());
        final ServerBootstrap bootstrap = new ServerBootstrap().channel(channelClass)
                .option(ChannelOption.SO_REUSEADDR, true)
                .group(this.bossGroup, this.workerGroup)
                .childHandler(this)
                .localAddress(address);

        // Enable tcp no delay
        if (config.isUseTcpNoDelay()) {
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        }

        this.checkExtraByte = !config.isDisableExtraByteCheck();

        // Bind the server
        try {
            this.channel = bootstrap.bind().syncUninterruptibly().channel();
        } catch (final Exception exception) {
            LOGGER.error("Failed to bind server", exception);
            System.exit(-1);
            return;
        }
        LOGGER.info("Listening on {}:{}", address.getAddress().getHostAddress(), address.getPort());
    }

    @Override
    public void stop() {
        if (this.channel != null) {
            try {
                this.channel.close().await(3, TimeUnit.SECONDS);
            } catch (final InterruptedException exception) {
                LOGGER.error("Netty server does not shutdown correctly", exception);
            }
        }
        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully(100L, 3_000L, TimeUnit.MILLISECONDS);
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully(100L, 3_000L, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void initChannel(final @NotNull Channel channel) {
        this.group.add(channel);
        final PlayerConnectionImpl connection = new PlayerConnectionImpl(channel);
        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("timeout", new ReadTimeoutHandler(30))
                .addLast("splitter", new VarIntFrameDecoder())
                .addLast("decoder", new PacketDecoder(connection, this.checkExtraByte))
                .addLast("prepender", FRAME_ENCODER);
        pipeline.addLast("encoder", new PacketEncoder(connection)).addLast("handler", connection);
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public ChannelGroup getGroup() {
        return this.group;
    }

}