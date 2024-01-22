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

package be.darkkraft.transferproxy.network.connection;

import be.darkkraft.transferproxy.api.network.connection.ConnectionState;
import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.api.network.packet.built.BuiltPacket;
import be.darkkraft.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import be.darkkraft.transferproxy.api.profile.ClientInformation;
import be.darkkraft.transferproxy.network.packet.config.clientbound.TransferPacket;
import be.darkkraft.transferproxy.network.packet.login.clientbound.LoginSuccessPacket;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.unix.Errors;
import io.netty.handler.timeout.ReadTimeoutException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.Objects;
import java.util.UUID;

public class PlayerConnectionImpl extends SimpleChannelInboundHandler<ServerboundPacket> implements PlayerConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerConnectionImpl.class);

    private final Channel channel;

    private ConnectionState state = ConnectionState.HANDSHAKE;
    private int protocol;
    private String hostname;
    private int hostPort;

    private String name;
    private UUID uuid;
    private ClientInformation information;
    private boolean fromTransfer;

    public PlayerConnectionImpl(final @NotNull Channel channel) {
        this.channel = Objects.requireNonNull(channel, "Channel cannot be null");
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final ServerboundPacket packet) {
        packet.handle(this);
    }

    @Override
    public void transfer(final @NotNull String host, final int hostPort) {
        this.ensureState(ConnectionState.CONFIG, "transfer");
        this.sendPacket(new TransferPacket(host, hostPort));
        LOGGER.info("Player {} are transferred to {}:{}", this.getDisplay(), host, hostPort);
        this.state = ConnectionState.CLOSED;
    }

    @Override
    public void sendLoginSuccess(final UUID uuid, final @NotNull String username) {
        this.ensureState(ConnectionState.LOGIN, "sendLoginSuccess");
        this.sendPacket(new LoginSuccessPacket(uuid, username, null));
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        if (this.state.isLogin()) {
            LOGGER.info("Player {} disconnected on state {}", this.getDisplay(), this.state);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (!this.channel.isOpen()) {
            return;
        }

        this.forceDisconnect();

        if (cause instanceof ReadTimeoutException) {
            LOGGER.info("Player {} has timed out", this.getDisplay());
            return;
        }

        // Ignore normal disconnect exception
        if (cause instanceof Errors.NativeIoException ||
                (cause instanceof SocketException && cause.getMessage().equals("Connection reset"))) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.error("Player {} disconnect for exception: {}", this.getDisplay(), cause.getMessage(), cause);
        } else {
            LOGGER.info("Player {} disconnect for exception: {}", this.getDisplay(), cause.getMessage());
        }
    }

    @Override
    public void sendPacket(final @NotNull Packet packet) {
        Objects.requireNonNull(packet, "The packet cannot be sent because it is null");
        if (this.channel.isActive() && this.state != ConnectionState.CLOSED) {
            this.channel.writeAndFlush(ensurePacket(this.channel.alloc(), packet), this.channel.voidPromise());
        }
    }

    @Override
    public void sendPacketAndClose(final @NotNull Packet packet) {
        Objects.requireNonNull(packet, "The packet cannot be sent because it is null");
        if (this.channel.isActive() && this.state != ConnectionState.CLOSED) {
            this.channel.writeAndFlush(ensurePacket(this.channel.alloc(), packet)).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void forceDisconnect() {
        if (this.channel.isOpen()) {
            this.channel.close();
            this.state = ConnectionState.CLOSED;
        }
    }

    @Override
    public void setInformation(final @NotNull ClientInformation information) {
        this.information = Objects.requireNonNull(information, "Information cannot be null");
    }

    @Override
    public void setProfile(final @NotNull String name, final @NotNull UUID uuid) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.uuid = Objects.requireNonNull(uuid, "UUID cannot be null");
    }

    @Override
    public void setState(@NotNull ConnectionState state) {
        if (Objects.requireNonNull(state, "State cannot be null") == ConnectionState.TRANSFER) {
            this.fromTransfer = true;
            state = ConnectionState.LOGIN;
        }
        this.state = state;
        if (this.state == ConnectionState.CONFIG) {
            LOGGER.info("Player {} are now connected", this.getDisplay());
        }
    }

    @Override
    public void setProtocol(final int protocol) {
        this.protocol = protocol;
    }

    @Override
    public void setHost(final @NotNull String hostname, final int hostPort) {
        this.hostname = Objects.requireNonNull(hostname, "Hostname cannot be null");
        this.hostPort = hostPort;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public ClientInformation getInformation() {
        return this.information;
    }

    @Override
    public @NotNull Channel getChannel() {
        return this.channel;
    }

    @Override
    public @NotNull ConnectionState getState() {
        return this.state;
    }

    @Override
    public int getProtocol() {
        return this.protocol;
    }

    @Override
    public @NotNull String getHostname() {
        return this.hostname;
    }

    @Override
    public int getHostPort() {
        return this.hostPort;
    }

    @Override
    public boolean isFromTransfer() {
        return this.fromTransfer;
    }

    private String getDisplay() {
        if (this.name != null) {
            return this.name;
        } else if (this.uuid != null) {
            return this.uuid.toString();
        }
        return Objects.requireNonNullElse(this.channel.remoteAddress(), this.channel).toString();
    }

    private static Object ensurePacket(final @NotNull ByteBufAllocator allocator, final @NotNull Object packet) {
        return packet instanceof final BuiltPacket built ? built.get(allocator) : packet;
    }

    private void ensureState(final @NotNull ConnectionState state, final String method) {
        if (this.state != state) {
            throw new IllegalStateException(
                    "PlayerConnection#" + method + " must be called on " + state + " state (current=" + this.state + ")");
        }
    }

}