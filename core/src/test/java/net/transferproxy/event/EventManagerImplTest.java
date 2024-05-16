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

package net.transferproxy.event;

import net.transferproxy.api.event.EventManager;
import net.transferproxy.api.event.EventType;
import net.transferproxy.api.event.listener.EventListener;
import net.transferproxy.api.util.test.MockedTransferProxy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventManagerImplTest {

    private EventManager instance;

    @BeforeAll
    static void setUpBeforeClass() {
        MockedTransferProxy.mock();
    }

    @BeforeEach
    void setUp() {
        this.instance = new EventManagerImpl();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testManyMethodsWithNullParameters() {
        // Test call()
        assertThrows(RuntimeException.class, () -> this.instance.call(null, null));
        assertThrows(RuntimeException.class, () -> this.instance.call(EventType.HANDSHAKE, null));

        final Object event = new Object();
        assertThrows(RuntimeException.class, () -> this.instance.call(null, event));

        // Test addListener()
        final EventListener<?> listener = ignored -> {
        };
        assertThrows(RuntimeException.class, () -> this.instance.addListener(null, null));
        assertThrows(RuntimeException.class, () -> this.instance.addListener(EventType.HANDSHAKE, null));
        assertThrows(RuntimeException.class, () -> this.instance.addListener(null, listener));

        // Test removeListener()
        assertFalse(this.instance.removeListener(null, null));
        assertFalse(this.instance.removeListener(EventType.HANDSHAKE, null));
        assertFalse(this.instance.removeListener(null, listener));
    }

    @Test
    void testGetListenersWithNullType() {
        assertNull(this.instance.getListeners(null));
    }

    @Test
    void testRemoveListenerWithNullParameters() {
        assertFalse(this.instance.removeListener(null, null));
        assertFalse(this.instance.removeListener(EventType.HANDSHAKE, null));
        assertFalse(this.instance.removeListener(null, ignored -> {
        }));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ParameterizedTest
    @EnumSource(EventType.class)
    void testCommonUseCaseOfAllMethods(final EventType type) {
        // TODO: Maybe should be moved into integration tests?
        // Mock listener and event
        final EventListener listener = mock(EventListener.class);
        final Object event = mock(type.getEventClass());

        // Assert listeners is empty
        assertNull(this.instance.getListeners(type));
        // Add listener and call with mocked event
        this.instance.addListener(type, listener);
        this.instance.call(type, event);
        // Verify listener is called one time
        verify(listener).handle(event);

        assertArrayEquals(this.instance.getListeners(type), new EventListener[] {listener});

        assertTrue(this.instance.removeListener(type, listener));
        final Object[] listeners = this.instance.getListeners(type);
        assertTrue(listeners == null || listeners.length == 0);

        this.instance.call(type, event);
        verify(listener).handle(event);
    }

}