/*
 * MIT License
 *
 * Copyright (c) 2026 Yvan Mazy
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

package net.transferproxy.test.agent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.transferproxy.test.agent.callback.StatusResponseCallback;
import net.transferproxy.test.common.SimpleStatusResponse;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public final class TestAgentImpl extends UnicastRemoteObject implements TestAgent {

    private final ServerStatusPinger serverStatusPinger = new ServerStatusPinger();

    TestAgentImpl() throws RemoteException {
        super();
    }

    @Override
    public void connect(final String host, final int port) throws RemoteException {
        Minecraft.getInstance().schedule(() -> {
            final Minecraft minecraft = Minecraft.getInstance();
            final ServerAddress address = new ServerAddress(host, port);
            ConnectScreen.startConnecting(new JoinMultiplayerScreen(new TitleScreen()),
                    minecraft,
                    address,
                    new ServerData("Test", address.toString(), ServerData.Type.OTHER),
                    false,
                    null);
        });
    }

    @Override
    public void disconnectServer() throws RemoteException {
        final CountDownLatch latch = new CountDownLatch(1);
        Minecraft.getInstance().schedule(() -> {
            try {
                Minecraft.getInstance().disconnect(new TitleScreen(), false);
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public SimpleStatusResponse requestStatus(final String host, final int port) throws RemoteException {
        final ServerData serverData = new ServerData("Test", host + ":" + port, ServerData.Type.OTHER);
        final CompletableFuture<SimpleStatusResponse> future = new CompletableFuture<>();
        try {
            this.serverStatusPinger.pingServer(serverData,
                    () -> {
                    },
                    new StatusResponseCallback(serverData, future),
                    EventLoopGroupHolder.remote(Minecraft.getInstance().options.useNativeTransport()));
        } catch (final UnknownHostException e) {
            future.completeExceptionally(e);
        }
        return future.join();
    }

    @Override
    public void updateClientOptions(final boolean test) throws RemoteException {
        final Options options = Minecraft.getInstance().options;
        if (test) {
            options.languageCode = "fr_fr";
            options.renderDistance().set(3);
            options.setModelPart(PlayerModelPart.LEFT_SLEEVE, false);
            options.chatVisibility().set(ChatVisiblity.SYSTEM);
            options.chatColors().set(false);
            options.mainHand().set(HumanoidArm.LEFT);
            options.particles().set(ParticleStatus.DECREASED);
        } else {
            options.languageCode = "en_us";
            options.renderDistance().set(12);
            options.setModelPart(PlayerModelPart.LEFT_SLEEVE, true);
            options.chatVisibility().set(ChatVisiblity.FULL);
            options.chatColors().set(true);
            options.mainHand().set(HumanoidArm.RIGHT);
            options.particles().set(ParticleStatus.ALL);
        }
    }

    @Override
    public boolean isLoaded() throws RemoteException {
        return true;
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public boolean isReady() throws RemoteException {
        final Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.isGameLoadFinished();
    }

}