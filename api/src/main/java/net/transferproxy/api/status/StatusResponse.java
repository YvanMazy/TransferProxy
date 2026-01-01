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

package net.transferproxy.api.status;

import net.kyori.adventure.text.Component;
import net.transferproxy.api.util.IOUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;

public record StatusResponse(Component description, Players players, Version version, String favicon) {

    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Contract("-> new")
    public @NotNull Builder toBuilder() {
        return new Builder(this);
    }

    public record Version(String name, int protocol) {

    }

    public record Players(int max, int online, SampleEntry[] sample) {

        public record SampleEntry(String name, String id) {

        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final Players players = (Players) o;
            return this.max == players.max && this.online == players.online && Arrays.equals(this.sample, players.sample);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(this.max, this.online);
            result = 31 * result + Arrays.hashCode(this.sample);
            return result;
        }

        @Override
        public String toString() {
            return "Players{max=" + this.max + ", online=" + this.online + ", sample=" + Arrays.toString(this.sample) + '}';
        }

    }

    public static class Builder {

        private Component description;

        private String name;
        private int protocol;

        private int online;
        private int max;
        private final List<Players.SampleEntry> entries = new ArrayList<>();

        private String favicon;

        private Builder() {
        }

        private Builder(final @NotNull StatusResponse response) {
            this.description = response.description;
            if (response.version != null) {
                this.name = response.version.name();
                this.protocol = response.version.protocol();
            }
            if (response.players != null) {
                this.online = response.players.online();
                this.max = response.players.max();
                final Players.SampleEntry[] sample = response.players.sample();
                if (sample != null && sample.length > 0) {
                    this.entries.addAll(List.of(sample));
                }
            }
            this.favicon = response.favicon();
        }

        @Contract("-> new")
        public @NotNull StatusResponse build() {
            if (this.name == null) {
                throw new IllegalStateException("Name must be set before building");
            }
            return new StatusResponse(this.description,
                    new Players(this.max, this.online, !this.entries.isEmpty() ? this.entries.toArray(Players.SampleEntry[]::new) : null),
                    new Version(this.name, this.protocol),
                    this.favicon);
        }

        @Contract("_ -> this")
        public @NotNull Builder description(final @Nullable Component component) {
            this.description = component;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder name(final @NotNull String name) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder protocol(final int protocol) {
            this.protocol = protocol;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder online(final int online) {
            this.online = online;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder max(final int max) {
            this.max = max;
            return this;
        }

        @Contract("_, _ -> this")
        public @NotNull Builder addEntry(final @NotNull String name, final @NotNull UUID uuid) {
            return this.addEntry(name, uuid.toString());
        }

        @Contract("_, _ -> this")
        public @NotNull Builder addEntry(final @NotNull String name, final @NotNull String uuid) {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(uuid, "uuid must not be null");
            this.entries.add(new Players.SampleEntry(name, uuid));
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addEntry(final @NotNull Players.SampleEntry entry) {
            Objects.requireNonNull(entry, "entry must not be null");
            this.entries.add(entry);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addEntries(final @NotNull Players.SampleEntry... entries) {
            Objects.requireNonNull(entries, "entries must not be null");
            this.entries.addAll(List.of(entries));
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder favicon(final @Nullable String base64) {
            this.favicon = base64;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder favicon(final @Nullable Path path) throws IOException {
            if (path == null) {
                this.favicon = null;
                return this;
            }
            return this.favicon(IOUtil.createImage(path));
        }

        @Contract("_ -> this")
        public @NotNull Builder uncheckedFavicon(final @Nullable Path path) {
            try {
                return this.favicon(path);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }

    }

}