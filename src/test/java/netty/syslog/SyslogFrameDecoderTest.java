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

        final ByteBuf buf = Unpooled.buffer(4096);

        // Encode octetCount frame
        ByteBufUtil.writeUtf8(buf, Integer.toString(octetCount.length()));
        buf.writeByte(' ');
        ByteBufUtil.writeUtf8(buf, octetCount);

        // Encode lfTerminated frame
        ByteBufUtil.writeUtf8(buf, lfTerminated);
        buf.writeByte('\n');

        // Encode nulTerminated frame
        ByteBufUtil.writeUtf8(buf, nulTerminated);
        buf.writeByte(0);

        // Encode crLfTerminated frame
        ByteBufUtil.writeUtf8(buf, crLfTerminated);
        buf.writeByte('\r');
        buf.writeByte('\n');

        // Encode second octetCount frame
        ByteBufUtil.writeUtf8(buf, Integer.toString(octetCount2.length()));
        buf.writeByte(' ');
        ByteBufUtil.writeUtf8(buf, octetCount2);

        // Run codec test
        new CodecTester()
                .fragmentBuffer(true)
                .decoderHandlers(new SyslogFrameDecoder(), new StringDecoder())
                .expect(buf, octetCount, lfTerminated, nulTerminated, crLfTerminated, octetCount2)
                .verify();
    }

}
