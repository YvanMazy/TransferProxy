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

import net.kyori.adventure.key.Key;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.configuration.ProxyConfiguration;
import net.transferproxy.api.event.EventType;
import net.transferproxy.api.event.listener.ReadyListener;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.test.util.ByteUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JoinTransferIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JoinTransferIntegrationTest.class);

    private static final int TEST_LOG_AMOUNT = 3;
    private static final Phaser PHASER = new Phaser(2);
    private static final AtomicBoolean SUCCESS = new AtomicBoolean();

    private static final Key AMOUNT_KEY = Key.key("transferproxy", "log_amount");

    private static String host;
    private static int hostPort;

    @BeforeAll
    static void beforeAll() {
        final TransferProxy proxy = TransferProxy.getInstance();

        final ProxyConfiguration.Network network = proxy.getConfiguration().getNetwork();
        host = network.getBindAddress();
        hostPort = network.getBindPort();

        proxy.getModuleManager().getEventManager().<ReadyListener>addListener(EventType.READY, JoinTransferIntegrationTest::onReady);
    }

    @Test
    @Timeout(value = 30L, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testMultipleReconnectWithCookieCounterAndTransfer() throws RemoteException {
        // Connect to proxy
        LOGGER.info("Connecting client to proxy...");
        IntegrationHandler.getMinecraft().agent().connect(host, hostPort);

        // Await proxy receive connection
        LOGGER.info("Waiting for proxy to receive connection...");
        final int amount = TEST_LOG_AMOUNT + 1;
        for (int i = 0; i < amount; i++) {
            PHASER.arriveAndAwaitAdvance();
        }
        assertTrue(SUCCESS.get());
    }

    private static void onReady(final PlayerConnection connection) {
        connection.fetchCookie(AMOUNT_KEY)
                .thenApply(data -> data != null ? ByteUtil.toInt(data) : null)
                .orTimeout(5L, TimeUnit.SECONDS)
                .thenAccept(amount -> onCookieReceived(connection, amount));
    }

    private static void onCookieReceived(final PlayerConnection connection, final Integer amount) {
        if (amount != null && amount == TEST_LOG_AMOUNT) {
            connection.forceDisconnect();
            SUCCESS.set(true);
            PHASER.arriveAndAwaitAdvance();
        } else {
            PHASER.arriveAndAwaitAdvance();
            connection.storeCookie(AMOUNT_KEY, ByteUtil.toByteArray(amount != null ? amount + 1 : 1));
            connection.transfer(host, hostPort);
        }
    }

}