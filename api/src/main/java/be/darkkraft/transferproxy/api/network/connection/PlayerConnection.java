package be.darkkraft.transferproxy.api.network.connection;

import be.darkkraft.transferproxy.api.network.packet.Packet;
import be.darkkraft.transferproxy.api.profile.ClientInformation;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PlayerConnection {

    void transfer(final @NotNull String host, final int hostPort);

    void sendPacket(final @NotNull Packet packet);

    void sendPacketAndClose(final @NotNull Packet packet);

    void forceDisconnect();

    String getName();

    UUID getUUID();

    ClientInformation getInformation();

    @NotNull Channel getChannel();

    @NotNull ConnectionState getState();

    int getProtocol();

    @NotNull String getHostname();

    int getHostPort();

    void setInformation(final @NotNull ClientInformation information);

    void setProfile(final @NotNull String name, final @NotNull UUID uuid);

    void setState(final @NotNull ConnectionState state);

    void setProtocol(final int protocol);

    void setHost(final @NotNull String hostname, final int hostPort);

}