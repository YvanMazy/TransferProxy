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

package net.transferproxy.api.network.packet.provider;

import io.netty.buffer.ByteBuf;
import net.transferproxy.api.network.packet.Packet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public final class PacketProviders {

    public static PacketProvider @NotNull [] providers(final @Nullable PacketProviders.OnlyBufferProvider provider) {
        return providers(provider != null ? provider.toProvider() : null);
    }

    public static PacketProvider @NotNull [] providers(final @Nullable PacketProvider provider) {
        return newBuilder().put(provider).build();
    }

    public static PacketProvider @NotNull [] providers(final @Nullable PacketProviders.OnlyBufferProvider... providers) {
        return Stream.of(providers).map(p -> p != null ? p.toProvider() : null).toArray(PacketProvider[]::new);
    }

    @Contract("-> new")
    public static @NotNull Builder newBuilder() {
        return new Builder();
    }

    private PacketProviders() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate this class");
    }

    public static class Builder {

        private final List<PacketProvider> providers = new ArrayList<>();

        @Contract("-> this")
        public @NotNull Builder putNull() {
            return this.put(null);
        }

        @Contract("_ -> this")
        public @NotNull Builder putOnlyBuffer(final @Nullable OnlyBufferProvider provider) {
            return this.put(provider != null ? provider.toProvider() : null);
        }

        @Contract("_ -> this")
        public @NotNull Builder put(final @Nullable PacketProvider provider) {
            this.providers.add(provider);
            return this;
        }

        @Contract("-> new")
        public PacketProvider @NotNull [] build() {
            return this.providers.toArray(PacketProvider[]::new);
        }

    }

    public interface OnlyBufferProvider extends Function<ByteBuf, Packet> {

        @NotNull
        default PacketProvider toProvider() {
            return (connection, buf) -> this.apply(buf);
        }

    }

}