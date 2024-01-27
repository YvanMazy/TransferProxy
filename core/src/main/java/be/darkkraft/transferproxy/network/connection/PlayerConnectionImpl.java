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
import be.darkkraft.transferproxy.api.status.StatusResponse;
import be.darkkraft.transferproxy.api.util.CookieUtil;
import be.darkkraft.transferproxy.network.packet.config.clientbound.ConfigCookieRequestPacket;
import be.darkkraft.transferproxy.network.packet.config.clientbound.ConfigDisconnectPacket;
import be.darkkraft.transferproxy.network.packet.config.clientbound.StoreCookiePacket;
import be.darkkraft.transferproxy.network.packet.config.clientbound.TransferPacket;
import be.darkkraft.transferproxy.network.packet.login.clientbound.LoginCookieRequestPacket;
import be.darkkraft.transferproxy.network.packet.login.clientbound.LoginDisconnectPacket;
import be.darkkraft.transferproxy.network.packet.login.clientbound.LoginSuccessPacket;
import be.darkkraft.transferproxy.network.packet.status.clientbound.StatusResponsePacket;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.unix.Errors;
import io.netty.handler.timeout.ReadTimeoutException;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerConnectionImpl extends SimpleChannelInboundHandler<ServerboundPacket> implements PlayerConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerConnectionImpl.class);

    private final Channel channel;

    private ConnectionState state = ConnectionState.HANDSHAKE;
    private int protocol;
    private String hostname;
    private int hostPort;

    private volatile Map<String, CompletableFuture<byte[]>> pendingCookies;

    private String name;
    private UUID uuid;
    private ClientInformation information;
    private boolean fromTransfer;

    public PlayerConnectionImpl(final @NotNull Channel channel) {
        this.channel = Objects.requireNonNull(channel, "channel cannot be null");
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
    public void sendLoginSuccess(final @NotNull UUID uuid, final @NotNull String username) {
        this.ensureState(ConnectionState.LOGIN, "sendLoginSuccess");
        Objects.requireNonNull(uuid, "uuid cannot be null");
        Objects.requireNonNull(username, "username cannot be null");
        this.sendPacket(new LoginSuccessPacket(uuid, username, null));
    }

    @Override
    public void sendStatusResponse(final @NotNull StatusResponse response) {
        this.ensureState(ConnectionState.STATUS, "sendStatusResponse");
        Objects.requireNonNull(response, "response cannot be null");
        this.sendPacket(new StatusResponsePacket(response));
    }

    @Override
    public synchronized CompletableFuture<byte[]> fetchCookie(final @NotNull String cookieKey) {
        Objects.requireNonNull(cookieKey, "Cookie key cannot be null");
        CookieUtil.ensureCookieFormat(cookieKey);
        if (this.state != ConnectionState.LOGIN && this.state != ConnectionState.CONFIG) {
            throw new IllegalStateException("Invalid state to fetch cookie " + this.state + " (cookie key=" + cookieKey + ")");
        }
        CompletableFuture<byte[]> future;
        if (this.pendingCookies == null) {
            this.pendingCookies = new HashMap<>();
            future = null;
        } else {
            future = this.pendingCookies.get(cookieKey);
        }
        if (future == null) {
            future = new CompletableFuture<>();
            this.pendingCookies.put(cookieKey, future);
            this.sendPacket(this.state == ConnectionState.LOGIN ?
                    new LoginCookieRequestPacket(cookieKey) :
                    new ConfigCookieRequestPacket(cookieKey));
        }
        return future;
    }

    @Override
    public void storeCookie(final @NotNull String cookieKey, final byte @NotNull [] payload) {
        Objects.requireNonNull(cookieKey, "Cookie key cannot be null");
        Objects.requireNonNull(payload, "Cookie payload cannot be null");
        CookieUtil.ensureCookieFormat(cookieKey);
        this.ensureState(ConnectionState.CONFIG, "storeCookie");
        this.sendPacket(new StoreCookiePacket(cookieKey, payload));
    }

    @Override
    public void handleCookieResponse(final @NotNull String cookieKey, final byte @Nullable [] payload) {
        if (this.pendingCookies == null) {
            return;
        }
        final CompletableFuture<byte[]> future = this.pendingCookies.get(cookieKey);
        if (future == null) {
            return;
        }
        future.complete(payload);
    }

    @Override
    public void disconnect(final @NotNull Component reason) {
        Objects.requireNonNull(reason, "reason cannot be null");
        if (this.state != ConnectionState.LOGIN && this.state != ConnectionState.CONFIG) {
            throw new IllegalStateException("Invalid state to disconnect: " + this.state);
        }
        this.sendPacketAndClose(this.state == ConnectionState.LOGIN ?
                new LoginDisconnectPacket(reason) :
                new ConfigDisconnectPacket(reason));
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
        Objects.requireNonNull(packet, "packet cannot be null");
        if (this.channel.isActive() && this.state != ConnectionState.CLOSED) {
            this.channel.writeAndFlush(ensurePacket(this.channel.alloc(), packet), this.channel.voidPromise());
        }
    }

    @Override
    public void sendPacketAndClose(final @NotNull Packet packet) {
        Objects.requireNonNull(packet, "packet cannot be null");
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
        this.information = Objects.requireNonNull(information, "information cannot be null");
    }

    @Override
    public void setProfile(final @NotNull String name, final @NotNull UUID uuid) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.uuid = Objects.requireNonNull(uuid, "uuid cannot be null");
    }

    @Override
    public void setState(@NotNull ConnectionState state) {
        if (Objects.requireNonNull(state, "state cannot be null") == ConnectionState.TRANSFER) {
            this.fromTransfer = true;
            state = ConnectionState.LOGIN;
        }
        this.state = state;
        if (this.state == ConnectionState.CONFIG) {
            if (this.isFromTransfer()) {
                LOGGER.info("Player {} is now connected and comes from transfer", this.getDisplay());
                return;
            }
            LOGGER.info("Player {} is now connected", this.getDisplay());
        }
    }

    @Override
    public void setProtocol(final int protocol) {
        this.protocol = protocol;
    }

    @Override
    public void setHost(final @NotNull String hostname, final int hostPort) {
        this.hostname = Objects.requireNonNull(hostname, "hostname cannot be null");
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