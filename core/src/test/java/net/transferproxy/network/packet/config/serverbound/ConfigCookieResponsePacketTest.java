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

package net.transferproxy.network.packet.config.serverbound;

import net.transferproxy.api.util.CookieUtil;
import net.transferproxy.network.packet.PacketTestBase;
import org.junit.jupiter.api.Test;

class ConfigCookieResponsePacketTest extends PacketTestBase {

    @Test
    void testWriteReadConsistency() {
        this.testOnlyBuffer(new ConfigCookieResponsePacket("minecraft:cookie_key", new byte[] {1, 2, 3}), ConfigCookieResponsePacket::new);
    }

    @Test
    void testWriteReadConsistencyWithNoValue() {
        this.testOnlyBuffer(new ConfigCookieResponsePacket("minecraft:cookie_key", null), ConfigCookieResponsePacket::new);
    }

    @Test
    void testWriteReadWithTooLongKey() {
        this.testFailOnlyBuffer(new ConfigCookieResponsePacket("minecraft:" + "a".repeat(Short.MAX_VALUE - 9), new byte[5]),
                ConfigCookieResponsePacket::new);
    }

    @Test
    void testWriteReadWithTooLongData() {
        this.testFailOnlyBuffer(new ConfigCookieResponsePacket("minecraft:cookie_key", new byte[CookieUtil.getMaxCookieSize() + 1]),
                ConfigCookieResponsePacket::new);
    }

}