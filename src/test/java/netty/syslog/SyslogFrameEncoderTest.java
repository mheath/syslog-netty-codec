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
import io.netty.handler.codec.string.StringEncoder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author Mike Heath
 */
public class SyslogFrameEncoderTest {

    @Test
    public void frameEncoder() {
        final String frame1 = "Have a nice day.";
        final String frame2 = "Your mom goes to college";
        final String frame3 = "This is just a test.";

        final ByteBuf expectedBuffer = Unpooled.buffer();
        writeFrame(expectedBuffer, frame1);
        writeFrame(expectedBuffer, frame2);
        writeFrame(expectedBuffer, frame3);

        new CodecTester()
                .encoderHandlers(new SyslogFrameEncoder(), new StringEncoder(StandardCharsets.UTF_8))
                .expect(expectedBuffer, frame1, frame2, frame3)
                .verify();
    }

    private void writeFrame(ByteBuf buffer, String frame) {
        ByteBufUtil.writeUtf8(buffer, Integer.toString(frame.length()) + " " + frame);
    }
}
