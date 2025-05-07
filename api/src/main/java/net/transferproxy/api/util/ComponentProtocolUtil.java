/*
 * MIT License
 *
 * Copyright (c) 2025 Yvan Mazy
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

package net.transferproxy.api.util;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer;
import net.kyori.option.OptionSchema;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// Credit: https://github.com/PaperMC/Velocity/blob/b411a0fa09f76aec062921b77642828408f0099f/proxy/src/main/java/com/velocitypowered/proxy/protocol/ProtocolUtils.java
public final class ComponentProtocolUtil {

    private static final GsonComponentSerializer PRE_1_16_SERIALIZER = GsonComponentSerializer.builder()
            .downsampleColors()
            .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
            .options(OptionSchema.globalSchema()
                    .stateBuilder()
                    // before 1.16
                    .value(JSONOptions.EMIT_RGB, Boolean.FALSE)
                    .value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.VALUE_FIELD)
                    // before 1.20.3
                    .value(JSONOptions.EMIT_COMPACT_TEXT_COMPONENT, Boolean.FALSE)
                    .value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, Boolean.FALSE)
                    .value(JSONOptions.VALIDATE_STRICT_EVENTS, Boolean.FALSE)
                    .build())
            .build();
    private static final GsonComponentSerializer PRE_1_20_3_SERIALIZER = GsonComponentSerializer.builder()
            .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
            .options(OptionSchema.globalSchema()
                    .stateBuilder()
                    // after 1.16
                    .value(JSONOptions.EMIT_RGB, Boolean.TRUE)
                    .value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.CAMEL_CASE)
                    .value(JSONOptions.EMIT_CLICK_EVENT_TYPE, JSONOptions.ClickEventValueMode.CAMEL_CASE)
                    // before 1.20.3
                    .value(JSONOptions.EMIT_COMPACT_TEXT_COMPONENT, Boolean.FALSE)
                    .value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, Boolean.FALSE)
                    .value(JSONOptions.VALIDATE_STRICT_EVENTS, Boolean.FALSE)
                    .build())
            .build();
    private static final GsonComponentSerializer PRE_1_21_5_SERIALIZER = GsonComponentSerializer.builder()
            .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
            .options(OptionSchema.globalSchema()
                    .stateBuilder()
                    // after 1.16
                    .value(JSONOptions.EMIT_RGB, Boolean.TRUE)
                    .value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.CAMEL_CASE)
                    .value(JSONOptions.EMIT_CLICK_EVENT_TYPE, JSONOptions.ClickEventValueMode.CAMEL_CASE)
                    // after 1.20.3
                    .value(JSONOptions.EMIT_COMPACT_TEXT_COMPONENT, Boolean.TRUE)
                    .value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, Boolean.TRUE)
                    .value(JSONOptions.VALIDATE_STRICT_EVENTS, Boolean.TRUE)
                    .build())
            .build();
    private static final GsonComponentSerializer MODERN_SERIALIZER = GsonComponentSerializer.builder()
            .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
            .options(OptionSchema.globalSchema()
                    .stateBuilder()
                    // after 1.16
                    .value(JSONOptions.EMIT_RGB, Boolean.TRUE)
                    .value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.SNAKE_CASE)
                    .value(JSONOptions.EMIT_CLICK_EVENT_TYPE, JSONOptions.ClickEventValueMode.SNAKE_CASE)
                    // after 1.20.3
                    .value(JSONOptions.EMIT_COMPACT_TEXT_COMPONENT, Boolean.TRUE)
                    .value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, Boolean.TRUE)
                    // after 1.21.5
                    .value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_KEY_AS_TYPE_AND_UUID_AS_ID, Boolean.FALSE)
                    .value(JSONOptions.VALIDATE_STRICT_EVENTS, Boolean.TRUE)
                    .build())
            .build();

    private static final int[] SERIALIZER_PROTOCOLS = {734, 735, 765, 770};

    private ComponentProtocolUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    @Contract(pure = true)
    public static @NotNull GsonComponentSerializer getPre116Serializer() {
        return PRE_1_16_SERIALIZER;
    }

    @Contract(pure = true)
    public static @NotNull GsonComponentSerializer getPre1203Serializer() {
        return PRE_1_20_3_SERIALIZER;
    }

    @Contract(pure = true)
    public static @NotNull GsonComponentSerializer getPre1215Serializer() {
        return PRE_1_21_5_SERIALIZER;
    }

    @Contract(pure = true)
    public static @NotNull GsonComponentSerializer getModernSerializer() {
        return MODERN_SERIALIZER;
    }

    @Contract(pure = true)
    public static @NotNull GsonComponentSerializer getSerializer(final int protocol) {
        if (protocol >= 770) { // >= 1.21.5
            return getModernSerializer();
        }
        if (protocol >= 765) { // >= 1.20.3
            return getPre1215Serializer();
        }
        if (protocol >= 735) { // >= 1.16
            return getPre1203Serializer();
        }
        return getPre116Serializer();
    }

    @Contract(pure = true)
    public static int[] getSerializerProtocols() {
        return SERIALIZER_PROTOCOLS;
    }

}