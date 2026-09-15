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

package net.transferproxy.network.packet.built;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import net.transferproxy.api.TransferProxy;
import net.transferproxy.api.network.connection.ConnectionState;
import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.api.network.packet.built.ProtocolizedBuiltPacket;
import net.transferproxy.api.network.packet.provider.PacketProvider;
import net.transferproxy.api.network.packet.provider.PacketProviderGroup;
import net.transferproxy.api.network.protocol.Protocolized;
import net.transferproxy.api.util.test.MockedTransferProxy;
import net.transferproxy.network.packet.PacketTestBase;
import net.transferproxy.network.packet.provider.PacketProviderGroups;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.IntFunction;

import static net.transferproxy.util.BufUtil.readVarInt;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProtocolizedBuiltPacketImplTest extends PacketTestBase {

    @BeforeAll
    static void setUpProxy() {
        MockedTransferProxy.mock();
    }

    @BeforeEach
    void setUpGroupFunction() {
        mockGroupFunction(PacketProviderGroups::determineGroup);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDeprecatedMethodThrowException() {
        final ProtocolizedBuiltPacket builtPacket = new ProtocolizedBuiltPacketImpl(new DummyTestPacket(5, "test"), false, 5);

        final ByteBuf buf = Unpooled.buffer();
        final Protocolized protocolized = Protocolized.of(5);
        assertThrows(RuntimeException.class, builtPacket::getId);
        assertThrows(RuntimeException.class, () -> builtPacket.write(buf));
        assertThrows(RuntimeException.class, () -> builtPacket.write(protocolized, buf));
    }

    @Test
    void testFindTheCorrectLowest() {
        final var builtPacket = new ProtocolizedBuiltPacketImpl(i -> new DummyTestPacket(i, "test data"), true, 5, 10, 15);

        final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
        record Entry(int protocol, int expected) {

        }
        // @formatter:off
        for (final Entry entry : new Entry[] {
                new Entry(6, 5),
                new Entry(9, 5),
                new Entry(10, 10),
                new Entry(12, 10),
                new Entry(0, 5),
                new Entry(100, 15)
        }) {
            final ByteBuf buf = assertDoesNotThrow(() -> builtPacket.get(allocator, entry.protocol));
            assertEquals(DummyTestPacket.ID, readVarInt(buf));

            final DummyTestPacket packet = assertDoesNotThrow(() -> new DummyTestPacket(buf));
            assertEquals("test data", packet.data());
            assertEquals(entry.expected, packet.protocol());

            buf.release();
        }
        // @formatter:on
    }

    @Test
    void testBufferIsCorrectlyCached() {
        final var builtPacket = spy(new ProtocolizedBuiltPacketImpl(i -> new DummyTestPacket(i, "test data"), true, 5, 10, 15));

        final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

        for (int i = 0; i < 20; i++) {
            final int protocol = i;
            assertDoesNotThrow(() -> builtPacket.get(allocator, protocol)).release();
        }

        verify(builtPacket, times(3)).computeBytes(anyInt(), anyInt(), any(ByteBufAllocator.class));
    }

    @Test
    void testPacketIdIsResolvedByGroup() {
        final int oldId = 0x10;
        final PacketProviderGroup oldGroup = new PacketProviderGroup() {
            @Override
            public PacketProvider @Nullable [] getProviders(final @NotNull ConnectionState state) {
                return null;
            }

            @Override
            public int getPacketId(final @NotNull Packet packet) {
                return oldId;
            }
        };
        final PacketProviderGroup newGroup = _ -> null;
        mockGroupFunction(protocol -> protocol >= 12 ? newGroup : oldGroup);

        final var builtPacket = spy(new ProtocolizedBuiltPacketImpl(i -> new DummyTestPacket(i, "test data"), true, 5, 10, 15));

        final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
        record Entry(int protocol, int expectedProtocol, int expectedId) {

        }
        // @formatter:off
        for (final Entry entry : new Entry[] {
                new Entry(11, 10, oldId),
                new Entry(12, 10, DummyTestPacket.ID),
                new Entry(10, 10, oldId),
                new Entry(13, 10, DummyTestPacket.ID),
                new Entry(15, 15, DummyTestPacket.ID),
                new Entry(5, 5, oldId)
        }) {
            final ByteBuf buf = assertDoesNotThrow(() -> builtPacket.get(allocator, entry.protocol));
            assertEquals(entry.expectedId, readVarInt(buf), "Wrong packet ID for protocol " + entry.protocol);

            final DummyTestPacket packet = assertDoesNotThrow(() -> new DummyTestPacket(buf));
            assertEquals(entry.expectedProtocol, packet.protocol());

            buf.release();
        }
        // @formatter:on

        // 10 is shared with 11, but not with 12 and 13 which are in another group
        verify(builtPacket, times(5)).computeBytes(anyInt(), anyInt(), any(ByteBufAllocator.class));
    }

    private static void mockGroupFunction(final @NotNull IntFunction<PacketProviderGroup> function) {
        when(TransferProxy.getInstance().getModuleManager().getPacketProviderGroupFunction()).thenReturn(function);
    }

}