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

package net.transferproxy.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public final class FormatUtil {

    private static final String SIZE_UNITS = "KMGTPE";

    private FormatUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    @Contract(pure = true)
    public static @NotNull String formatSize(final long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(1024));
        final String pre = SIZE_UNITS.charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    @Contract(pure = true)
    public static @NotNull String formatDuration(final @NotNull Duration duration) {
        final long days = duration.toDays();
        final long hours = duration.toHoursPart();
        final long minutes = duration.toMinutesPart();
        final long seconds = duration.toSecondsPart();

        final StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            builder.append(minutes).append("m ");
        }
        builder.append(seconds).append("s");

        return builder.toString();
    }

}