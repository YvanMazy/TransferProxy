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

package net.transferproxy.network.packet.config.serverbound;

import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.profile.ChatVisibility;
import net.transferproxy.api.profile.ClientInformation;
import net.transferproxy.api.profile.MainHand;
import net.transferproxy.api.profile.ParticleStatus;
import net.transferproxy.network.packet.PacketTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientInformationPacketTest extends PacketTestBase {

    private static PlayerConnection mockConnection;

    @BeforeAll
    static void beforeAll() {
        mockConnection = mock(PlayerConnection.class);
        when(mockConnection.getProtocol()).thenReturn(768);
    }

    @ParameterizedTest
    @MethodSource("generateInformation")
    void testWriteReadConsistency(final ClientInformation information) {
        this.test(new ClientInformationPacket(information), buf -> new ClientInformationPacket(mockConnection, buf));
    }

    public static Stream<ClientInformation> generateInformation() {
        return Stream.of(ChatVisibility.values())
                .flatMap(chatVisibility -> Stream.of(MainHand.values())
                        .flatMap(mainHand -> Stream.concat(Stream.of(ParticleStatus.values()), Stream.ofNullable(null))
                                .flatMap(particleStatus -> IntStream.range(0, 16)
                                        .mapToObj(i -> ClientInformation.create("en_US",
                                                (byte) 1,
                                                chatVisibility,
                                                (i & 1) != 0,
                                                (byte) 0,
                                                mainHand,
                                                (i & 2) != 0,
                                                (i & 4) != 0,
                                                particleStatus)))));
    }


}