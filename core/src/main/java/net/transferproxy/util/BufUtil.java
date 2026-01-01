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

package net.transferproxy.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

import static net.kyori.adventure.nbt.BinaryTagTypes.*;

public final class BufUtil {

    @SuppressWarnings("unchecked")
    private static final BinaryTagType<? extends BinaryTag>[] BINARY_TAG_TYPES =
            new BinaryTagType[] {END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY, STRING, LIST, COMPOUND, INT_ARRAY, LONG_ARRAY};

    private BufUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    public static void writeVarInt(final @NotNull ByteBuf buf, int value) {
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
            return;
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            buf.writeShort((value & 0x7F | 0x80) << 8 | (value >>> 7));
            return;
        }
        while ((value & 0xFFFFFF80) != 0x0) {
            buf.writeByte(value | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    public static void writeString(final @NotNull ByteBuf buf, final @NotNull CharSequence string) {
        writeString(buf, string, Short.MAX_VALUE);
    }

    public static void writeString(final @NotNull ByteBuf buf, final @NotNull CharSequence string, final int maxLength) {
        if (string == null) {
            throw new EncoderException("Input string must not be null");
        }
        if (string.length() > maxLength) {
            throw new EncoderException("Invalid string length: " + string.length() + " > " + maxLength);
        }

        final int writtenBytes = ByteBufUtil.utf8Bytes(string);

        writeVarInt(buf, writtenBytes);
        ByteBufUtil.writeUtf8(buf, string);
    }

    public static void writeUUID(final @NotNull ByteBuf buf, final @NotNull UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static void writeBytes(final @NotNull ByteBuf buf, final byte @NotNull [] payload, final int maxLength) {
        if (payload.length > maxLength) {
            throw new EncoderException("Invalid bytes length: " + payload.length + " > " + maxLength);
        }
        writeVarInt(buf, payload.length);
        buf.writeBytes(payload);
    }

    public static <T> void writeArray(final @NotNull ByteBuf buf,
                                      final @NotNull T[] array,
                                      final @NotNull BiConsumer<ByteBuf, T> consumer) {
        writeVarInt(buf, array.length);
        for (final T t : array) {
            consumer.accept(buf, t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends BinaryTag> void writeTag(final @NotNull ByteBuf buf, final @NotNull T tag) {
        final BinaryTagType<T> type = (BinaryTagType<T>) tag.type();
        buf.writeByte(type.id());
        try {
            type.write(tag, new ByteBufOutputStream(buf));
        } catch (final IOException e) {
            throw new EncoderException(e);
        }
    }

    public static int readVarInt(final @NotNull ByteBuf buf) {
        int value = 0;
        int length = 0;
        int part;
        do {
            part = buf.readByte();
            value |= (part & 0x7F) << (length++ * 7);
            if (length > 5) {
                throw new DecoderException("VarInt is too big");
            }
        } while (part < 0);
        return value;
    }

    public static String readString(final @NotNull ByteBuf buf) {
        return readString(buf, Short.MAX_VALUE);
    }

    public static String readString(final @NotNull ByteBuf buf, final int maxLength) {
        final int length = readVarInt(buf);
        if (length < 0) {
            throw new DecoderException("Invalid received string length: " + length);
        }

        final int maxBytes = ByteBufUtil.utf8MaxBytes(maxLength);
        if (length > maxBytes) {
            throw new DecoderException("Invalid received string length: " + length + " > " + maxBytes);
        }

        final int readable = buf.readableBytes();
        if (length > readable) {
            throw new DecoderException("Invalid received string in buffer: " + length + " > " + readable);
        }

        final String string = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
        buf.readerIndex(buf.readerIndex() + length);
        if (string.length() > maxLength) {
            throw new DecoderException("Invalid read string length: " + string.length() + " > " + maxLength);
        }

        return string;
    }

    public static UUID readUUID(final @NotNull ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static byte[] readBytes(final @NotNull ByteBuf buf, final int maxLength) {
        final int length = readVarInt(buf);
        if (length > maxLength) {
            throw new DecoderException("Invalid bytes length: " + length + "/" + maxLength);
        }
        final byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return bytes;
    }

    public static <T> T[] readArray(final @NotNull ByteBuf buf,
                                    final @NotNull IntFunction<T[]> arrayBuilder,
                                    final @NotNull Function<ByteBuf, T> objectBuilder,
                                    final int maxLength) {
        final int length = readVarInt(buf);
        if (length > maxLength) {
            throw new DecoderException("Invalid array length: " + length + "/" + maxLength);
        }
        final T[] array = arrayBuilder.apply(length);
        for (int i = 0; i < length; i++) {
            array[i] = objectBuilder.apply(buf);
        }
        return array;
    }

    public static BinaryTag readTag(final @NotNull ByteBuf buf) {
        final byte id = buf.readByte();
        if (id >= BINARY_TAG_TYPES.length) {
            throw new DecoderException("Invalid binary tag id: " + id);
        }
        final BinaryTagType<? extends BinaryTag> type = BINARY_TAG_TYPES[id];
        try {
            return type.read(new ByteBufInputStream(buf));
        } catch (final IOException e) {
            throw new DecoderException(e);
        }
    }

}