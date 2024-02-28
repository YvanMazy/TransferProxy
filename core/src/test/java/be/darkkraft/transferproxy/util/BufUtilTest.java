/*
 * MIT License
 *
 * Copyright (c) 2024 Darkkraft
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

package be.darkkraft.transferproxy.util;

import be.darkkraft.transferproxy.util.test.NBTTestUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.kyori.adventure.nbt.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BufUtilTest {

    private ByteBuf buf;

    @BeforeEach
    void setUp() {
        this.buf = Unpooled.buffer();
    }

    @Test
    void testReadingWithoutData() {
        assertThrows(IndexOutOfBoundsException.class, () -> BufUtil.readVarInt(this.buf));
        assertThrows(IndexOutOfBoundsException.class, () -> BufUtil.readString(this.buf));
        assertThrows(IndexOutOfBoundsException.class, () -> BufUtil.readUUID(this.buf));
        assertThrows(IndexOutOfBoundsException.class, () -> BufUtil.readBytes(this.buf, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> BufUtil.readTag(this.buf));
    }

    @ParameterizedTest
    @ValueSource(ints = {-127, 127, 0, 10, 500, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void testVarIntWriteReadConsistency(final int value) {
        assertDoesNotThrow(() -> BufUtil.writeVarInt(this.buf, value));
        assertEquals(value, BufUtil.readVarInt(this.buf));
        assertEquals(0, this.buf.readableBytes());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello, world!", "-"})
    void testStringWriteReadConsistency() {
        assertDoesNotThrow(() -> BufUtil.writeString(this.buf, "Hello, world!"));
        assertEquals("Hello, world!", BufUtil.readString(this.buf));
        assertEquals(0, this.buf.readableBytes());
    }

    @ParameterizedTest
    @MethodSource("generateUUIDs")
    void testUUIDWriteReadConsistency(final UUID value) {
        assertDoesNotThrow(() -> BufUtil.writeUUID(this.buf, value));
        assertEquals(value, BufUtil.readUUID(this.buf));
        assertEquals(0, this.buf.readableBytes());
    }

    @ParameterizedTest
    @MethodSource("generateBytes")
    void testBytesWriteReadConsistency(final byte[] value) {
        assertDoesNotThrow(() -> BufUtil.writeBytes(this.buf, value));
        assertArrayEquals(value, BufUtil.readBytes(this.buf, value.length));
        assertEquals(0, this.buf.readableBytes());
    }

    @ParameterizedTest
    @MethodSource("generateStrings")
    void testArrayWriteReadConsistency(final List<String> list) {
        final String[] value = list.toArray(String[]::new);
        assertDoesNotThrow(() -> BufUtil.writeArray(this.buf, value, BufUtil::writeString));
        assertArrayEquals(value, BufUtil.readArray(this.buf, String[]::new, BufUtil::readString, value.length));
        assertEquals(0, this.buf.readableBytes());
    }

    @ParameterizedTest
    @MethodSource("generateTags")
    void testTagWriteReadConsistency(final BinaryTag tag) {
        assertDoesNotThrow(() -> BufUtil.writeTag(this.buf, tag));
        assertEquals(tag, BufUtil.readTag(this.buf));
        assertEquals(0, this.buf.readableBytes());
    }

    @Test
    void testReadStringTooLong() {
        final String longString = "a".repeat(5);
        assertDoesNotThrow(() -> BufUtil.writeString(this.buf, longString));
        assertThrows(DecoderException.class, () -> BufUtil.readString(this.buf, 4));
    }

    @Test
    void testReadBytesTooLong() {
        final byte[] bytes = new byte[5];
        assertDoesNotThrow(() -> BufUtil.writeBytes(this.buf, bytes));
        assertThrows(DecoderException.class, () -> BufUtil.readBytes(this.buf, 4));
    }

    @Test
    void testReadInvalidTagId() {
        this.buf.writeByte(BinaryTagTypes.LONG_ARRAY.id() + 1);
        assertThrows(DecoderException.class, () -> BufUtil.readTag(this.buf));
    }

    @Test
    void testReadMalformedTag() {
        this.buf.writeByte(BinaryTagTypes.STRING.id());
        assertThrows(DecoderException.class, () -> BufUtil.readTag(this.buf));
    }

    @AfterEach
    void tearDown() {
        this.buf.release();
    }

    static Stream<UUID> generateUUIDs() {
        return Stream.generate(UUID::randomUUID).limit(5);
    }

    static Stream<byte[]> generateBytes() {
        return Stream.of(new byte[0], new byte[5], new byte[] {1, 2, 3}, new byte[] {-127, 127});
    }

    static Stream<List<String>> generateStrings() {
        return Stream.of(List.of(), List.of("test"), List.of("test1", "test2"));
    }

    static Stream<BinaryTag> generateTags() {
        return Stream.of(IntBinaryTag.intBinaryTag(0),
                StringBinaryTag.stringBinaryTag("test"),
                CompoundBinaryTag.empty(),
                CompoundBinaryTag.from(Map.of("key", IntBinaryTag.intBinaryTag(5), "key2", LongBinaryTag.longBinaryTag(100L))),
                NBTTestUtil.generateComplexCompoundTag());
    }

}