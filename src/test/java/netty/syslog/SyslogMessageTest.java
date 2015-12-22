/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package netty.syslog;

import org.junit.Test;

import static org.junit.Assert.*;

public class SyslogMessageTest {

    @Test
    public void utf8WithBom() {
        final String utf8String = "¬(¬α ∨ β)";
        final SyslogMessage message = SyslogMessage.builder()
                                                 .utf8Content(utf8String)
                                                 .build(false);
        assertEquals(utf8String, message.contentAsUtf8());
    }

    @Test
    public void printableUsAscii() {
        for (char c = 0; c < 33; c++) {
            assertFalse("With c = " + (int)c, SyslogMessage.isPrintableUsAscii(c));
        }
        for (char c = 33; c < 127; c++) {
            assertTrue("With c = " + (int)c, SyslogMessage.isPrintableUsAscii(c));
        }
        for (char c = 128; c < Character.MAX_VALUE; c++) {
            assertFalse("With c = " + (int)c, SyslogMessage.isPrintableUsAscii(c));
        }
    }

}
