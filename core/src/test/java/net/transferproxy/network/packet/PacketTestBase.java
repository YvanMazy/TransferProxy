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

package net.transferproxy.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.transferproxy.api.network.connection.PlayerConnection;
import net.transferproxy.api.network.packet.Packet;
import net.transferproxy.util.BufUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PacketTestBase {

    protected static PlayerConnection mockConnection;

    private ByteBuf buf;

    @BeforeAll
    static void beforeAll() {
        mockConnection = mock(PlayerConnection.class);
        when(mockConnection.getProtocol()).thenReturn(768);
    }

    @BeforeEach
    void setUp() {
        this.buf = Unpooled.buffer();
    }

    protected <T extends Packet> void testOnlyBuffer(final T packet, final Function<ByteBuf, T> builder) {
        this.test(packet, (conn, buf) -> builder.apply(buf));
    }

    protected <T extends Packet> void test(final T packet, final BiFunction<PlayerConnection, ByteBuf, T> builder) {
        packet.write(mockConnection, this.buf);
        assertEquals(packet, builder.apply(mockConnection, this.buf));
        assertEquals(0, this.buf.readableBytes());
    }

    protected <T extends Packet> void testFail(final T packet, final Function<ByteBuf, T> builder) {
        // Assert encoding failure
        assertThrows(EncoderException.class, () -> packet.write(mockConnection, this.buf));

        // Clear buffer in case data may have been written
        this.buf.clear();

        // Mock BufUtil for bypass length checking
        try (final MockedStatic<BufUtil> mockedStatic = mockStatic(BufUtil.class,
                Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))) {
            mockedStatic.when(() -> BufUtil.writeString(any(ByteBuf.class), anyString(), anyInt()))
                    .thenAnswer(PacketTestBase.<String>buildWriteAnswer(BufUtil::writeString));
            mockedStatic.when(() -> BufUtil.writeBytes(any(ByteBuf.class), any(byte[].class), anyInt()))
                    .thenAnswer(buildWriteAnswer(BufUtil::writeBytes));
            mockedStatic.when(() -> BufUtil.readString(any(ByteBuf.class), anyInt())).thenAnswer(buildReadAnswer(BufUtil::readString));
            mockedStatic.when(() -> BufUtil.readBytes(any(ByteBuf.class), anyInt())).thenAnswer(buildReadAnswer(BufUtil::readBytes));

            // Write with bypass
            packet.write(this.buf);
            // Check equality with bypass
            assertEquals(packet, builder.apply(this.buf));

            // Re-clear after write
            this.buf.clear();

            // Re-write with bypass
            packet.write(mockConnection, this.buf);
        }

        // Assert decoding failure
        assertThrows(DecoderException.class, () -> builder.apply(this.buf));
    }

    @AfterEach
    void tearDown() {
        this.buf.release();
    }

    private static <T> Answer<Object> buildWriteAnswer(final BufConsumer<T> consumer) {
        return invocation -> {
            if (invocation.<Integer>getArgument(2) == Integer.MAX_VALUE) {
                return invocation.callRealMethod();
            }
            consumer.accept(invocation.getArgument(0), invocation.getArgument(1), Integer.MAX_VALUE);
            return null;
        };
    }

    private static <T> Answer<Object> buildReadAnswer(final BiFunction<ByteBuf, Integer, T> consumer) {
        return invocation -> {
            if (invocation.<Integer>getArgument(1) == Integer.MAX_VALUE) {
                return invocation.callRealMethod();
            }
            return consumer.apply(invocation.getArgument(0), Integer.MAX_VALUE);
        };
    }

    private interface BufConsumer<T> {

        void accept(final ByteBuf buf, T value, final int newLimit);

    }

}