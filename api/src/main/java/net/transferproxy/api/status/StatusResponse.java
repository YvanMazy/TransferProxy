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

package net.transferproxy.api.status;

import net.transferproxy.api.util.IOUtil;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public record StatusResponse(Component description, Players players, Version version, String favicon) {

    public static Builder builder() {
        return new Builder();
    }

    public record Version(String name, int protocol) {

    }

    public record Players(int max, int online, SampleEntry[] sample) {

        public record SampleEntry(String name, UUID id) {

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

        public StatusResponse build() {
            return new StatusResponse(this.description,
                    new Players(this.max, this.online, this.entries.toArray(Players.SampleEntry[]::new)),
                    new Version(this.name, this.protocol),
                    this.favicon);
        }

        public Builder description(final Component component) {
            this.description = component;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder protocol(final int protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder online(final int online) {
            this.online = online;
            return this;
        }

        public Builder max(final int max) {
            this.max = max;
            return this;
        }

        public Builder addEntry(final String name, final UUID uuid) {
            this.entries.add(new Players.SampleEntry(name, uuid));
            return this;
        }

        public Builder addEntry(final Players.SampleEntry entry) {
            this.entries.add(entry);
            return this;
        }

        public Builder addEntries(final Players.SampleEntry... entries) {
            this.entries.addAll(List.of(entries));
            return this;
        }

        public Builder favicon(final String base64) {
            this.favicon = base64;
            return this;
        }

        public Builder favicon(final Path path) throws IOException {
            return this.favicon(IOUtil.createImage(path));
        }

    }

}