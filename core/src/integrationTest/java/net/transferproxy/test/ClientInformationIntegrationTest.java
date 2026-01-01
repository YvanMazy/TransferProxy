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

package net.transferproxy.test;

import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.configuration.ProxyConfiguration;
import net.transferproxy.api.event.EventType;
import net.transferproxy.api.event.listener.ReadyListener;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.profile.ChatVisibility;
import net.transferproxy.api.profile.ClientInformation;
import net.transferproxy.api.profile.MainHand;
import net.transferproxy.api.profile.ParticleStatus;
import net.transferproxy.test.agent.TestAgent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClientInformationIntegrationTest {

    private static String host;
    private static int hostPort;

    private static CompletableFuture<ClientInformation> informationFuture;

    @BeforeAll
    static void beforeAll() {
        final TransferProxy proxy = TransferProxy.getInstance();

        final ProxyConfiguration.Network network = proxy.getConfiguration().getNetwork();
        host = network.getBindAddress();
        hostPort = network.getBindPort();

        proxy.getModuleManager().getEventManager().<ReadyListener>addListener(EventType.READY, ClientInformationIntegrationTest::onReady);
    }

    @Test
    @Timeout(value = 30L, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testClientInformationReading() throws RemoteException {
        informationFuture = new CompletableFuture<>();
        final TestAgent agent = IntegrationHandler.getMinecraft().agent();
        agent.updateClientOptions(true);
        agent.connect(host, hostPort);

        final ClientInformation information = informationFuture.join();
        assertEquals("fr_fr", information.locale());
        assertEquals(3, information.viewDistance());
        assertEquals(ChatVisibility.SYSTEM, information.chatVisibility());
        assertFalse(information.chatColors());
        assertEquals(123, information.displayedSkinParts());
        assertEquals(MainHand.LEFT, information.mainHand());
        assertEquals(ParticleStatus.DECREASED, information.particleStatus());
    }

    private static void onReady(final @NotNull PlayerConnection connection) {
        informationFuture.complete(connection.getInformation());
    }

}