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

package net.transferproxy.api.resourcepack;

public enum ResourcePackResult {

    SUCCESS,
    DECLINED,
    FAILED_TO_DOWNLOAD,
    ACCEPTED,
    DOWNLOADED,
    INVALID_URL,
    FAILED_TO_RELOAD,
    DISCARDED;

    private final byte id = (byte) this.ordinal();

    public byte getId() {
        return this.id;
    }

    public static ResourcePackResult fromId(final int id) {
        return switch (id) {
            case 0 -> SUCCESS;
            case 1 -> DECLINED;
            case 2 -> FAILED_TO_DOWNLOAD;
            case 3 -> ACCEPTED;
            case 4 -> DOWNLOADED;
            case 5 -> INVALID_URL;
            case 6 -> FAILED_TO_RELOAD;
            case 7 -> DISCARDED;
            default -> throw new IllegalArgumentException("Invalid ResourcePackResult id: " + id);
        };
    }

}