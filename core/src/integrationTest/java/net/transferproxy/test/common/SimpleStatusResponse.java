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

package net.transferproxy.test.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public record SimpleStatusResponse(String description, Players players, Version version, String favicon) implements Serializable {

    public record Version(String name, int protocol) implements Serializable {

    }

    public record Players(int max, int online, Players.SampleEntry[] sample) implements Serializable {

        public record SampleEntry(String name, UUID id) implements Serializable {

        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final SimpleStatusResponse.Players players = (Players) o;
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

}