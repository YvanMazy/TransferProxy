package be.darkkraft.transferproxy.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class BufUtil {

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

    public static void writeString(final @NotNull ByteBuf buf, final @NotNull String string) {
        writeVarInt(buf, ByteBufUtil.utf8Bytes(string));
        ByteBufUtil.writeUtf8(buf, string);
    }

    public static void writeUUID(final @NotNull ByteBuf buf, final @NotNull UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
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
        if (length > maxLength * 3) {
            throw new DecoderException("Invalid String length (" + length + " > " + (maxLength * 3) + ")");
        } else if (length < 0) {
            throw new DecoderException("String with zero length is not valid");
        }

        final String string = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);

        buf.readerIndex(buf.readerIndex() + length);

        if (string.length() > maxLength) {
            throw new DecoderException("Invalid String length (" + string.length() + " > " + maxLength + ")");
        }

        return string;
    }

    public static UUID readUUID(final @NotNull ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

}