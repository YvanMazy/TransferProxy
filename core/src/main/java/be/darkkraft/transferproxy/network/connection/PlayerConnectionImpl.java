package be.darkkraft.transferproxy.network.connection;

import be.darkkraft.transferproxy.api.network.connection.ConnectionState;
import be.darkkraft.transferproxy.api.network.connection.PlayerConnection;
import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.api.network.packet.built.BuiltPacket;
import be.darkkraft.transferproxy.api.network.packet.serverbound.ServerboundPacket;
import be.darkkraft.transferproxy.api.profile.ClientInformation;
import be.darkkraft.transferproxy.network.packet.config.clientbound.TransferPacket;
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

    public PlayerConnectionImpl(final @NotNull Channel channel) {
        this.channel = Objects.requireNonNull(channel, "Channel cannot be null");
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final ServerboundPacket packet) {
        packet.handle(this);
    }

    @Override
    public void transfer(final @NotNull String host, final int hostPort) {
        if (this.state != ConnectionState.CONFIG) {
            throw new IllegalStateException("Connection cannot be transferred when state is not");
        }
        this.sendPacket(new TransferPacket(host, hostPort));
        LOGGER.info("Player {} are transferred to {}:{}", this.getDisplay(), host, hostPort);
        this.state = ConnectionState.CLOSED;
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
    public void setInformation(final @NotNull ClientInformation information) {
        this.information = Objects.requireNonNull(information, "Information cannot be null");
    }

    @Override
    public void setProfile(final @NotNull String name, final @NotNull UUID uuid) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.uuid = Objects.requireNonNull(uuid, "UUID cannot be null");
    }

    @Override
    public void setState(final @NotNull ConnectionState state) {
        this.state = Objects.requireNonNull(state, "State cannot be null");
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

}