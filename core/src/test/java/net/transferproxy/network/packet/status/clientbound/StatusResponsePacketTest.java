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

package net.transferproxy.network.packet.status.clientbound;

import net.transferproxy.api.status.StatusResponse;
import net.transferproxy.network.packet.PacketTestBase;
import net.transferproxy.util.test.TestGenerationUtil;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class StatusResponsePacketTest extends PacketTestBase {

    @Test
    void testWriteReadConsistency() {
        this.test(new StatusResponsePacket(StatusResponse.builder()
                .name("TransferProxy")
                .description(TestGenerationUtil.generateComplexComponent())
                .addEntry("Darkkraft", UUID.fromString("169033d6-0967-49dc-828e-a6c48665e08f"))
                .addEntry("Random", UUID.randomUUID())
                .max(Integer.MAX_VALUE)
                .online(Integer.MIN_VALUE)
                .online(0)
                .protocol(-1)
                .favicon("base64")
                .build()), StatusResponsePacket::new);
    }

}