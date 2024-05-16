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

package net.transferproxy.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;
import net.kyori.adventure.nbt.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Credit: https://github.com/PaperMC/Velocity/blob/b9b11665b9a3926bdbf45986e6e0f736ca0d01cd/proxy/src/main/java/com/velocitypowered/proxy/protocol/packet/chat/ComponentHolder.java
public final class NBTUtil {

    private NBTUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    public static BinaryTag serialize(final JsonElement json) {
        if (json instanceof final JsonPrimitive primitive) {
            return serializePrimitive(json, primitive);
        } else if (json instanceof final JsonObject object) {
            final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();

            for (final Map.Entry<String, JsonElement> property : object.entrySet()) {
                builder.put(property.getKey(), serialize(property.getValue()));
            }

            return builder.build();
        } else if (json instanceof final JsonArray array) {
            if (array.isEmpty()) {
                return ListBinaryTag.empty();
            }
            return serializeArray(array);
        }

        return EndBinaryTag.endBinaryTag();
    }

    private static @NotNull BinaryTag serializeArray(final @NotNull JsonArray array) {
        final List<BinaryTag> items = new ArrayList<>(array.size());
        BinaryTagType<? extends BinaryTag> listType = null;

        for (final JsonElement element : array) {
            final BinaryTag tag = serialize(element);
            items.add(tag);

            if (listType == null) {
                listType = tag.type();
            } else if (listType != tag.type()) {
                listType = BinaryTagTypes.COMPOUND;
            }
        }

        if (listType == null) {
            return EndBinaryTag.endBinaryTag();
        }

        switch (listType.id()) {
            case 1:
                final byte[] bytes = new byte[array.size()];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = (Byte) array.get(i).getAsNumber();
                }

                return ByteArrayBinaryTag.byteArrayBinaryTag(bytes);
            case 3:
                final int[] ints = new int[array.size()];
                for (int i = 0; i < ints.length; i++) {
                    ints[i] = (Integer) array.get(i).getAsNumber();
                }

                return IntArrayBinaryTag.intArrayBinaryTag(ints);
            case 4:
                final long[] longs = new long[array.size()];
                for (int i = 0; i < longs.length; i++) {
                    longs[i] = (Long) array.get(i).getAsNumber();
                }

                return LongArrayBinaryTag.longArrayBinaryTag(longs);
            case 10:
                items.replaceAll(tag -> tag.type() == BinaryTagTypes.COMPOUND ? tag : CompoundBinaryTag.builder().put("", tag).build());
                break;
            default: break;
        }

        return ListBinaryTag.listBinaryTag(listType, items);
    }

    private static @NotNull BinaryTag serializePrimitive(final @NotNull JsonElement json, final @NotNull JsonPrimitive primitive) {
        if (primitive.isNumber()) {
            final Number number = json.getAsNumber();

            if (number instanceof final Byte b) {
                return ByteBinaryTag.byteBinaryTag(b);
            } else if (number instanceof final Short s) {
                return ShortBinaryTag.shortBinaryTag(s);
            } else if (number instanceof final Integer i) {
                return IntBinaryTag.intBinaryTag(i);
            } else if (number instanceof final Long l) {
                return LongBinaryTag.longBinaryTag(l);
            } else if (number instanceof final Float f) {
                return FloatBinaryTag.floatBinaryTag(f);
            } else if (number instanceof final Double d) {
                return DoubleBinaryTag.doubleBinaryTag(d);
            } else if (number instanceof LazilyParsedNumber) {
                return IntBinaryTag.intBinaryTag(number.intValue());
            }
        } else if (primitive.isString()) {
            return StringBinaryTag.stringBinaryTag(primitive.getAsString());
        } else if (primitive.isBoolean()) {
            return ByteBinaryTag.byteBinaryTag((byte) (primitive.getAsBoolean() ? 1 : 0));
        }
        throw new IllegalArgumentException("Unknown json primitive: " + primitive);
    }

    public static JsonElement deserialize(final BinaryTag tag) {
        switch (tag.type().id()) {
            case 1:
                return new JsonPrimitive(((ByteBinaryTag) tag).value());
            case 2:
                return new JsonPrimitive(((ShortBinaryTag) tag).value());
            case 3:
                return new JsonPrimitive(((IntBinaryTag) tag).value());
            case 4:
                return new JsonPrimitive(((LongBinaryTag) tag).value());
            case 5:
                return new JsonPrimitive(((FloatBinaryTag) tag).value());
            case 6:
                return new JsonPrimitive(((DoubleBinaryTag) tag).value());
            case 7:
                final byte[] byteArray = ((ByteArrayBinaryTag) tag).value();

                final JsonArray byteJsonArray = new JsonArray(byteArray.length);
                for (final byte b : byteArray) {
                    byteJsonArray.add(new JsonPrimitive(b));
                }

                return byteJsonArray;
            case 8:
                return new JsonPrimitive(((StringBinaryTag) tag).value());
            case 9:
                final ListBinaryTag list = (ListBinaryTag) tag;
                final JsonArray itemArray = new JsonArray(list.size());

                for (final BinaryTag sub : list) {
                    itemArray.add(deserialize(sub));
                }

                return itemArray;
            case 10:
                final CompoundBinaryTag compound = (CompoundBinaryTag) tag;
                final JsonObject object = new JsonObject();

                for (final Map.Entry<String, ? extends BinaryTag> entry : compound) {
                    final String key = entry.getKey();
                    object.add(key.isEmpty() ? "text" : key, deserialize(entry.getValue()));
                }

                return object;
            case 11:
                final int[] intArray = ((IntArrayBinaryTag) tag).value();

                final JsonArray intJsonArray = new JsonArray(intArray.length);
                for (final int i : intArray) {
                    intJsonArray.add(new JsonPrimitive(i));
                }

                return intJsonArray;
            case 12:
                final long[] longArray = ((LongArrayBinaryTag) tag).value();

                final JsonArray longJsonArray = new JsonArray(longArray.length);
                for (final long l : longArray) {
                    longJsonArray.add(new JsonPrimitive(l));
                }

                return longJsonArray;
            default:
                throw new IllegalArgumentException("Unknown NBT tag: " + tag);
        }
    }

}