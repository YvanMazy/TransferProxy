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

package net.transferproxy.network.packet.login.clientbound;

import net.transferproxy.api.profile.Property;
import net.transferproxy.network.packet.PacketTestBase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class LoginSuccessPacketTest extends PacketTestBase {

    @Test
    void testWriteReadConsistency() {
        final UUID uuid = UUID.fromString("169033d6-0967-49dc-828e-a6c48665e08f");
        final String username = "Darkkraft";
        final Property[] properties = {new Property("name", "value", "signature")};
        this.test(new LoginSuccessPacket(uuid, username, new Property[0]), LoginSuccessPacket::new);
        this.test(new LoginSuccessPacket(uuid, username, properties), LoginSuccessPacket::new);
        mockClientProtocol(768);
        this.test(new LoginSuccessPacket(uuid, username, new Property[0]), LoginSuccessPacket::new);
        this.test(new LoginSuccessPacket(uuid, username, properties), LoginSuccessPacket::new);
    }

}