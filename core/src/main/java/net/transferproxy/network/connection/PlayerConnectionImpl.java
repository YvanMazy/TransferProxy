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

package net.transferproxy.network.connection;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.unix.Errors;
import io.netty.handler.timeout.ReadTimeoutException;
import net.kyori.adventure.text.Component;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.configuration.ProxyConfiguration;
import net.transferproxy.api.network.connection.ConnectionState;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.api.network.packet.built.BuiltPacket;
import net.transferproxy.api.network.packet.provider.PacketProviderGroup;
import net.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import net.transferproxy.api.profile.ClientInformation;
import net.transferproxy.api.status.StatusResponse;
import net.transferproxy.api.util.CookieUtil;
import net.transferproxy.network.packet.config.clientbound.*;
import net.transferproxy.network.packet.login.clientbound.LoginCookieRequestPacket;
import net.transferproxy.network.packet.login.clientbound.LoginDisconnectPacket;
import net.transferproxy.network.packet.login.clientbound.LoginSuccessPacket;
import net.transferproxy.network.packet.provider.PacketProviderGroups;
import net.transferproxy.network.packet.status.clientbound.StatusResponsePacket;
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
    private PacketProviderGroup packetProviderGroup;

    private ConnectionState state = ConnectionState.HANDSHAKE;
    private int protocol;
    private String hostname;
    private int hostPort;

    // This is the field that must be volatile and not the entries
    private volatile Map<String, CompletableFuture<byte[]>> pendingCookies;

    private String name;
    private UUID uuid;
    private ClientInformation information;
    private boolean fromTransfer;
    private String brand;

    public PlayerConnectionImpl(final @NotNull Channel channel) {
        this.channel = Objects.requireNonNull(channel, "channel must not be null");
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final ServerboundPacket packet) {
        packet.handle(this);
    }

    @Override
    public void transfer(final @NotNull String host, final int hostPort) {
        this.ensureState(ConnectionState.CONFIG, "transfer");
        this.sendPacket(new TransferPacket(host, hostPort));
        if (TransferProxy.getInstance().getConfiguration().getLogging().isLogTransfer()) {
            LOGGER.info("Player {} are transferred to {}:{}", this.getDisplay(), host, hostPort);
        }
        this.state = ConnectionState.CLOSED;
    }

    @Override
    public void sendLoginSuccess(final @NotNull UUID uuid, final @NotNull String username) {
        this.ensureState(ConnectionState.LOGIN, "sendLoginSuccess");
        Objects.requireNonNull(uuid, "uuid must not be null");
        Objects.requireNonNull(username, "username must not be null");
        this.sendPacket(new LoginSuccessPacket(uuid, username, null));
    }

    @Override
    public void sendStatusResponse(final @NotNull StatusResponse response) {
        this.ensureState(ConnectionState.STATUS, "sendStatusResponse");
        Objects.requireNonNull(response, "response must not be null");
        this.sendPacket(new StatusResponsePacket(response));
    }

    @Override
    public synchronized @NotNull CompletableFuture<byte[]> fetchCookie(final @NotNull String cookieKey) {
        Objects.requireNonNull(cookieKey, "Cookie key must not be null");
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
        Objects.requireNonNull(cookieKey, "Cookie key must not be null");
        Objects.requireNonNull(payload, "Cookie payload must not be null");
        CookieUtil.ensureCookieFormat(cookieKey);
        this.ensureState(ConnectionState.CONFIG, "storeCookie");
        if (payload.length > CookieUtil.getMaxCookieSize()) {
            throw new IllegalArgumentException("The cookie to store is too big: " + payload.length + " > " + CookieUtil.getMaxCookieSize());
        }
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
    public @NotNull Map<String, CompletableFuture<byte[]>> getPendingCookies() {
        return this.pendingCookies != null ? Map.copyOf(this.pendingCookies) : Map.of();
    }

    @Override
    public void removeResourcePack(final @Nullable UUID uuid) {
        this.sendPacket(new RemoveResourcePackPacket(uuid));
    }

    @Override
    public void resetChat() {
        this.sendPacket(ResetChatPacket.INSTANCE);
    }

    @Override
    public void disconnect(final @NotNull Component reason) {
        Objects.requireNonNull(reason, "reason must not be null");
        if (this.state != ConnectionState.LOGIN && this.state != ConnectionState.CONFIG) {
            throw new IllegalStateException("Invalid state to disconnect: " + this.state);
        }
        this.sendPacketAndClose(
                this.state == ConnectionState.LOGIN ? new LoginDisconnectPacket(reason) : new ConfigDisconnectPacket(reason));
    }

    @Override
    public void channelInactive(final @NotNull ChannelHandlerContext ctx) {
        if (this.state.isLogin() && TransferProxy.getInstance().getConfiguration().getLogging().isLogDisconnect()) {
            LOGGER.info("Player {} disconnected on state {}", this.getDisplay(), this.state);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (!this.channel.isOpen()) {
            return;
        }

        this.forceDisconnect();

        final ProxyConfiguration.Logging logging = TransferProxy.getInstance().getConfiguration().getLogging();
        if (cause instanceof ReadTimeoutException) {
            if (logging.isLogTimeout()) {
                LOGGER.info("Player {} has timed out", this.getDisplay());
            }
            return;
        }

        // Ignore normal disconnect exception
        if (cause instanceof Errors.NativeIoException ||
                (cause instanceof SocketException && cause.getMessage().equals("Connection reset"))) {
            return;
        }

        if (logging.isLogDisconnectForException()) {
            if (logging.isLogCompleteDisconnectException()) {
                LOGGER.error("Player {} disconnect for exception: {}", this.getDisplay(), cause.getMessage(), cause);
            } else {
                LOGGER.info("Player {} disconnect for exception: {}", this.getDisplay(), cause.getMessage());
            }
        }
    }

    @Override
    public void sendPacket(final @NotNull Packet packet) {
        Objects.requireNonNull(packet, "packet must not be null");
        if (this.channel.isActive() && this.state != ConnectionState.CLOSED) {
            this.channel.writeAndFlush(ensurePacket(this.channel.alloc(), packet), this.channel.voidPromise());
        }
    }

    @Override
    public void sendPacketAndClose(final @NotNull Packet packet) {
        Objects.requireNonNull(packet, "packet must not be null");
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
        this.information = Objects.requireNonNull(information, "information must not be null");
    }

    @Override
    public void setProfile(final @NotNull String name, final @NotNull UUID uuid) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.uuid = Objects.requireNonNull(uuid, "uuid must not be null");
    }

    @Override
    public void setState(@NotNull ConnectionState state) {
        if (Objects.requireNonNull(state, "state must not be null") == ConnectionState.TRANSFER) {
            this.fromTransfer = true;
            state = ConnectionState.LOGIN;
        }
        this.state = state;
        if (this.state == ConnectionState.CONFIG && TransferProxy.getInstance().getConfiguration().getLogging().isLogConnect()) {
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
        this.packetProviderGroup = TransferProxy.getInstance().getModuleManager().getPacketProviderGroupFunction().apply(protocol);
    }

    @Override
    public void setHost(final @NotNull String hostname, final int hostPort) {
        this.hostname = Objects.requireNonNull(hostname, "hostname must not be null");
        this.hostPort = hostPort;
    }

    @Override
    public void setBrand(final @Nullable String brand) {
        this.brand = brand;
    }

    @Override
    public void setPacketProviderGroup(final @NotNull PacketProviderGroup packetProviderGroup) {
        this.packetProviderGroup = Objects.requireNonNull(packetProviderGroup, "packetProviderGroup must not be null");
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
    @NotNull
    public PacketProviderGroup getPacketProviderGroup() {
        return Objects.requireNonNullElse(this.packetProviderGroup, PacketProviderGroups.getDefaultGroup());
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
    public @Nullable String getBrand() {
        return this.brand;
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