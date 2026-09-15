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

package net.transferproxy.network.packet.provider;

import net.transferproxy.api.network.connection.ConnectionState;
import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.api.network.packet.provider.PacketProviderGroup;
import net.transferproxy.network.packet.config.KeepAlivePacket;
import net.transferproxy.network.packet.config.clientbound.CodeOfConductPacket;
import net.transferproxy.network.packet.config.clientbound.ServerSelectKnownPacksPacket;
import net.transferproxy.network.packet.config.clientbound.StoreCookiePacket;
import net.transferproxy.network.packet.config.clientbound.TransferPacket;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PacketProviderGroupsTest {

    private static final int V26_2_PROTOCOL = 776;
    private static final int V26_3_PROTOCOL = 777;

    @Test
    void testDetermineGroup() {
        final PacketProviderGroup latest = PacketProviderGroups.getDefaultGroup();
        assertSame(latest, PacketProviderGroups.determineGroup(V26_3_PROTOCOL));
        assertSame(latest, PacketProviderGroups.determineGroup(Integer.MAX_VALUE));
        assertNotSame(latest, PacketProviderGroups.determineGroup(V26_2_PROTOCOL));
        assertNotSame(latest, PacketProviderGroups.determineGroup(766)); // 766 = 1.20.5
    }

    @Test
    void testClientboundPacketIds() {
        final PacketProviderGroup v26_2 = PacketProviderGroups.determineGroup(V26_2_PROTOCOL);
        final PacketProviderGroup v26_3 = PacketProviderGroups.determineGroup(V26_3_PROTOCOL);
        record Entry(Packet packet, int v26_2Id, int v26_3Id) {

        }
        // @formatter:off
        for (final Entry entry : new Entry[] {
                new Entry(new KeepAlivePacket(0L), 0x04, 0x04),
                new Entry(new StoreCookiePacket("minecraft:key", new byte[0]), 0x0A, 0x0B),
                new Entry(new TransferPacket("localhost", 25565), 0x0B, 0x0C),
                new Entry(new ServerSelectKnownPacksPacket(List.of()), 0x0E, 0x0F),
                new Entry(new CodeOfConductPacket("code"), 0x13, 0x14)
        }) {
            final String name = entry.packet.getClass().getSimpleName();
            assertEquals(entry.v26_2Id, v26_2.getPacketId(entry.packet), "Wrong 26.2 ID for " + name);
            assertEquals(entry.v26_3Id, v26_3.getPacketId(entry.packet), "Wrong 26.3 ID for " + name);
        }
        // @formatter:on
    }

    @Test
    void testServerboundProvidersAreUnchangedIn26_3() {
        final PacketProviderGroup v26_2 = PacketProviderGroups.determineGroup(V26_2_PROTOCOL);
        final PacketProviderGroup v26_3 = PacketProviderGroups.determineGroup(V26_3_PROTOCOL);
        for (final ConnectionState state : ConnectionState.values()) {
            assertSame(v26_3.getProviders(state), v26_2.getProviders(state), "Different providers for state: " + state);
        }
    }

}