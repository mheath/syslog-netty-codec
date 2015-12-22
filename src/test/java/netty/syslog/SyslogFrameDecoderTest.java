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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.string.StringDecoder;
import org.junit.Test;

public class SyslogFrameDecoderTest {

    @Test
    public void frameDecoding() {
        final String octetCount = "RFC 6587 3.5.1 Octet counting encoded frame";
        final String lfTerminated = "RFC 6587 3.5.1 LF terminated frame";
        final String nulTerminated = "RFC 6587 3.5.1 NUL terminated frame";
        final String crLfTerminated = "RFC 6587 3.5.1 CR LF terminated frame ";
        final String octetCount2 = "Another Octet counted frame";

        final ByteBuf buffer = Unpooled.buffer(4096);

        // Encode octetCount frame
        ByteBufUtil.writeUtf8(buffer, Integer.toString(octetCount.length()));
        buffer.writeByte(' ');
        ByteBufUtil.writeUtf8(buffer, octetCount);

        // Encode lfTerminated frame
        ByteBufUtil.writeUtf8(buffer, lfTerminated);
        buffer.writeByte('\n');

        // Encode nulTerminated frame
        ByteBufUtil.writeUtf8(buffer, nulTerminated);
        buffer.writeByte(0);

        // Encode crLfTerminated frame
        ByteBufUtil.writeUtf8(buffer, crLfTerminated);
        buffer.writeByte('\r');
        buffer.writeByte('\n');

        // Encode second octetCount frame
        ByteBufUtil.writeUtf8(buffer, Integer.toString(octetCount2.length()));
        buffer.writeByte(' ');
        ByteBufUtil.writeUtf8(buffer, octetCount2);

        // Run codec test
        new CodecTester()
                .fragmentBuffer(true)
                .decoderHandlers(new SyslogFrameDecoder(), new StringDecoder())
                .expect(buffer, octetCount, lfTerminated, nulTerminated, crLfTerminated, octetCount2)
                .verify();
    }

}
