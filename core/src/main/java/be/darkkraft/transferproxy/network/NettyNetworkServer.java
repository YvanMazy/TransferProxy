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

package be.darkkraft.transferproxy.network;

import be.darkkraft.transferproxy.api.TransferProxy;
import be.darkkraft.transferproxy.api.configuration.ProxyConfiguration;
import be.darkkraft.transferproxy.api.network.NetworkServer;
import be.darkkraft.transferproxy.network.connection.PlayerConnectionImpl;
import be.darkkraft.transferproxy.network.frame.clientbound.PacketEncoder;
import be.darkkraft.transferproxy.network.frame.clientbound.VarIntFrameEncoder;
import be.darkkraft.transferproxy.network.frame.serverbound.PacketDecoder;
import be.darkkraft.transferproxy.network.frame.serverbound.VarIntFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class NettyNetworkServer extends ChannelInitializer<Channel> implements NetworkServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyNetworkServer.class);
    private static final ChannelHandler FRAME_ENCODER = new VarIntFrameEncoder();
    private static final ChannelHandler PACKET_ENCODER = new PacketEncoder();

    private Channel channel;

    @Override
    public void start() {
        final ProxyConfiguration.Network config = TransferProxy.getInstance().getConfiguration().getNetwork();
        ResourceLeakDetector.setLevel(config.getResourceLeakDetectorLevel());

        final Class<? extends ServerChannel> channelClass;

        final EventLoopGroup bossGroup;
        final EventLoopGroup workerGroup;

        final DefaultThreadFactory bossFactory = new DefaultThreadFactory("Boss Thread");
        final DefaultThreadFactory workerFactory = new DefaultThreadFactory("Worker Thread");

        final int bossThread = config.getBossThreads();
        final int workerThread = config.getWorkerThreads();

        // Use Epoll if available
        if (config.useEpoll() && Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(bossThread, bossFactory);
            workerGroup = new EpollEventLoopGroup(workerThread, workerFactory);
            channelClass = EpollServerSocketChannel.class;
            LOGGER.info("The network will use the EPOLL channel type");
        } else {
            bossGroup = new NioEventLoopGroup(bossThread, bossFactory);
            workerGroup = new NioEventLoopGroup(workerThread, workerFactory);
            channelClass = NioServerSocketChannel.class;
            LOGGER.info("The network will use the NIO channel type");
        }

        final InetSocketAddress address = new InetSocketAddress(config.getBindAddress(), config.getBindPort());
        final ServerBootstrap bootstrap = new ServerBootstrap().channel(channelClass)
                .option(ChannelOption.SO_REUSEADDR, true)
                .group(bossGroup, workerGroup)
                .childHandler(this)
                .localAddress(address);

        // Enable tcp no delay
        if (config.useTcpNoDelay()) {
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        }

        // Bind the server
        this.channel = bootstrap.bind().syncUninterruptibly().channel();
        LOGGER.info("Listening on " + address.getAddress().getHostAddress() + ":" + address.getPort());
    }

    @Override
    public void stop() {
        if (this.channel != null) {
            try {
                this.channel.close().await(10, TimeUnit.SECONDS);
            } catch (final InterruptedException exception) {
                LOGGER.error("Netty server does not shutdown correctly", exception);
            }
        }
    }

    @Override
    protected void initChannel(final Channel channel) {
        final PlayerConnectionImpl connection = new PlayerConnectionImpl(channel);
        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("timeout", new ReadTimeoutHandler(30))
                .addLast("splitter", new VarIntFrameDecoder())
                .addLast("decoder", new PacketDecoder(connection))
                .addLast("prepender", FRAME_ENCODER);
        pipeline.addLast("encoder", PACKET_ENCODER).addLast("handler", connection);
    }

}