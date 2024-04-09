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

package be.darkkraft.transferproxy.api.plugin.info;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class PluginInfo {

    private String name;
    private String description;
    private String version;
    private String author;
    private String main;

    public PluginInfo(final @NotNull String name,
                      final String description,
                      final @NotNull String version,
                      final @NotNull String author,
                      final @NotNull String main) {
        this.name = Objects.requireNonNull(name, "Plugin name must not be null");
        this.description = Objects.requireNonNullElse(description, "");
        this.version = Objects.requireNonNull(version, "Plugin version must not be null");
        this.author = Objects.requireNonNull(author, "Plugin author must not be null");
        this.main = Objects.requireNonNull(main, "Plugin main must not be null");
    }

    public PluginInfo() {
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull String getDescription() {
        return this.description;
    }

    public @NotNull String getVersion() {
        return this.version;
    }

    public @NotNull String getAuthor() {
        return this.author;
    }

    public @NotNull String getMain() {
        return this.main;
    }

    @Override
    public String toString() {
        return "PluginInfo{" + "name='" + this.name + '\'' + ", description='" + this.description + '\'' + ", version='" + this.version +
                '\'' + ", author='" + this.author + '\'' + ", main='" + this.main + '\'' + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final PluginInfo that = (PluginInfo) o;
        return Objects.equals(this.name, that.name) && Objects.equals(this.description, that.description) &&
                Objects.equals(this.version, that.version) && Objects.equals(this.author, that.author) &&
                Objects.equals(this.main, that.main);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.description, this.version, this.author, this.main);
    }

}