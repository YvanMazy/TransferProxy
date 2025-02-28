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

package net.transferproxy.test;

import net.kyori.adventure.text.Component;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.configuration.ProxyConfiguration;
import net.transferproxy.api.event.EventType;
import net.transferproxy.api.status.StatusResponse;
import net.transferproxy.status.SnapshotStatusListener;
import net.transferproxy.test.common.SimpleStatusResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.rmi.RemoteException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusDisplayIntegrationTest {

    private static String host;
    private static int hostPort;

    private static SimpleStatusResponse statusResponse;

    @BeforeAll
    static void beforeAll() {
        final TransferProxy proxy = TransferProxy.getInstance();

        final ProxyConfiguration.Network network = proxy.getConfiguration().getNetwork();
        host = network.getBindAddress();
        hostPort = network.getBindPort();

        final String description = "Hello world!";
        final String name = "Test";
        final int protocol = -13;
        final int online = 25;
        final int max = 201;
        final String entryName = "entry-name";
        final UUID entryId = UUID.randomUUID();
        final String favicon =
                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M+AHgADzwJ+p7ft3QAAAABJRU5ErkJggg==";
        statusResponse = new SimpleStatusResponse(description,
                new SimpleStatusResponse.Players(online,
                        max,
                        new SimpleStatusResponse.Players.SampleEntry[] {new SimpleStatusResponse.Players.SampleEntry(entryName, entryId)}),
                new SimpleStatusResponse.Version(name, protocol),
                favicon);
        proxy.getModuleManager()
                .getEventManager()
                .addListener(EventType.STATUS,
                        new SnapshotStatusListener(StatusResponse.builder()
                                .name(name)
                                .protocol(protocol)
                                .online(online)
                                .max(max)
                                .description(Component.text(description))
                                .addEntry(entryName, entryId)
                                .favicon(favicon)
                                .build()));
    }

    @Test
    @Timeout(value = 30L, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testStatusResponseIntegrity() throws RemoteException {
        assertEquals(statusResponse, IntegrationHandler.getMinecraft().agent().requestStatus(host, hostPort));
    }

}