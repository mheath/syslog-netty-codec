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

import io.netty.buffer.Unpooled;
import netty.syslog.SyslogMessage.Facility;
import netty.syslog.SyslogMessage.Severity;
import org.junit.Test;

import java.time.ZonedDateTime;

public class SyslogMessageEncoderTest {

    @Test
    public void encode() throws Exception {
        final SyslogMessage message = new SyslogMessage.MessageBuilder()
                .facility(SyslogMessage.Facility.USER_LEVEL)
                .severity(SyslogMessage.Severity.INFORMATION)
                .timestamp(ZonedDateTime.parse("2014-03-20T20:01:02.000001Z"))
                .hostname("loggregator")
                .applicationName("20d38e29-85bb-4833-81c8-99ba7d0c1b09")
                .processId("[App/0]")
                .content(Unpooled.wrappedBuffer("SHLVL : 1".getBytes()))
                .build(true);
        new CodecTester()
                .encoderHandlers(new SyslogMessageEncoder())
                .expect(Unpooled.wrappedBuffer(
                        "<14>1 2014-03-20T20:01:02.000001Z loggregator 20d38e29-85bb-4833-81c8-99ba7d0c1b09 [App/0] - - SHLVL : 1"
                                .getBytes()), message)
                .verify();

    }

    @Test
    public void structuredDataEmptyValue() {
        final SyslogMessage message = new SyslogMessage.MessageBuilder()
                .facility(Facility.KERNEL)
                .severity(Severity.DEBUG)
                .addStructuredDataElement("foo")
                .build(true);
        new CodecTester()
                .encoderHandlers(new SyslogMessageEncoder())
                .expect(Unpooled.wrappedBuffer(
                        "<7>1 - - - - - [foo -]"
                                .getBytes()), message)
                .verify();
    }

    @Test
    public void structuredDataSingleValue() {
        final SyslogMessage message = new SyslogMessage.MessageBuilder()
                .facility(Facility.KERNEL)
                .severity(Severity.DEBUG)
                .addStructuredDataElement("bar", "a", "1")
                .build(true);
        new CodecTester()
                .encoderHandlers(new SyslogMessageEncoder())
                .expect(Unpooled.wrappedBuffer(
                        "<7>1 - - - - - [bar a=\"1\"]"
                                .getBytes()), message)
                .verify();
    }

}
