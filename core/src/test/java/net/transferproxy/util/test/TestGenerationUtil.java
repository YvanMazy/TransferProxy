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

package net.transferproxy.util.test;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class TestGenerationUtil {

    private TestGenerationUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    public static BinaryTag generateComplexCompoundTag() {
        return CompoundBinaryTag.builder()
                .put("compound", CompoundBinaryTag.builder().put("sub", IntBinaryTag.intBinaryTag(1)).build())
                .putBoolean("boolean", true)
                .putByte("byte", Byte.MAX_VALUE)
                .putShort("short", Short.MAX_VALUE)
                .putInt("int", Integer.MAX_VALUE)
                .putLong("long", Long.MAX_VALUE)
                .putFloat("float", Float.MAX_VALUE)
                .putDouble("double", Double.MAX_VALUE)
                .putByteArray("byte_array", new byte[] {Byte.MAX_VALUE})
                .putString("string", "test")
                .putIntArray("int_array", new int[] {Integer.MAX_VALUE})
                .putLongArray("long_array", new long[] {Long.MAX_VALUE})
                .build();
    }

    public static Component generateComplexComponent() {
        return MiniMessage.miniMessage()
                .deserialize("<color:#FF5555>This is a <color:#55FF55>test! <rainbow:!2>||||||||||||||||||||||||</rainbow>");
    }

}