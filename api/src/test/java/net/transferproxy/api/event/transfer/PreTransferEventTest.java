/*
 * MIT License
 *
 * Copyright (c) 2026 Yvan Mazy
 * Copyright (c) 2026 Pectics
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

package net.transferproxy.api.event.transfer;

import net.transferproxy.api.network.connection.PlayerConnection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class PreTransferEventTest {

    @Test
    void testFieldsAndMutability() {
        final PlayerConnection connection = mock(PlayerConnection.class);
        final PreTransferEvent event = new PreTransferEvent(connection, "backend.example.com", 25566);

        assertSame(connection, event.getConnection());
        assertEquals("backend.example.com", event.getHost());
        assertEquals(25566, event.getHostPort());

        event.setHost("other.example.com");
        event.setHostPort(25567);
        assertEquals("other.example.com", event.getHost());
        assertEquals(25567, event.getHostPort());
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void testNullParameters() {
        assertThrows(NullPointerException.class, () -> new PreTransferEvent(null, "host", 25565));

        final PlayerConnection connection = mock(PlayerConnection.class);
        assertThrows(NullPointerException.class, () -> new PreTransferEvent(connection, null, 25565));

        final PreTransferEvent event = new PreTransferEvent(connection, "host", 25565);
        assertThrows(NullPointerException.class, () -> event.setHost(null));
    }

}
