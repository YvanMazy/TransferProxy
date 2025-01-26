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

import static net.kyori.adventure.key.Key.checkNamespace;
import static net.kyori.adventure.key.Key.checkValue;

public final class CookieUtil {

    private static final int MAX_COOKIE_SIZE = 5120; // 5 kiB

    private CookieUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    /**
     * Ensures the format of the given cookie key is valid.
     *
     * @param cookieKey the cookie key to validate
     *
     * @throws IllegalArgumentException if the cookie key is null,
     *                                  does not contain a colon separator,
     *                                  or contains invalid characters in the namespace or value
     */
    public static void ensureCookieFormat(final String cookieKey) {
        if (cookieKey == null) {
            throw new IllegalArgumentException("Cookie key must not be null");
        }
        final int index = cookieKey.indexOf(':');
        if (index < 1) {
            throw new IllegalArgumentException("Cookie key format must be: namespace:key");
        }
        if (checkNamespace(cookieKey.substring(0, index)).isPresent()) {
            throw new IllegalArgumentException("Invalid characters in cookie namespace: " + cookieKey);
        }
        if (checkValue(cookieKey.substring(index + 1)).isPresent()) {
            throw new IllegalArgumentException("Invalid characters in cookie value: " + cookieKey);
        }
    }

    public static int getMaxCookieSize() { // Maybe that changes in future versions?
        return MAX_COOKIE_SIZE;
    }

}