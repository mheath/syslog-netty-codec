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
import io.netty.util.AsciiString;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SyslogMessageDecoderTest {

    @Test
    public void decode() throws Exception {

        final SyslogMessage message = new SyslogMessage.MessageBuilder()
                .facility(SyslogMessage.Facility.USER_LEVEL)
                .severity(SyslogMessage.Severity.INFORMATION)
                .timestamp(ZonedDateTime.parse("2014-03-20T20:14:14Z"))
                .hostname("loggregator")
                .applicationName("20d38e29-85bb-4833-81c8-99ba7d0c1b09")
                .processId("[App/0]")
                .content(Unpooled.wrappedBuffer("SHLVL : 1".getBytes()))
                .build(true);
        new CodecTester()
                .decoderHandlers(new SyslogMessageDecoder())
                .expect(Unpooled.wrappedBuffer(
                        "<14>1 2014-03-20T20:14:14+00:00 loggregator 20d38e29-85bb-4833-81c8-99ba7d0c1b09 [App/0] - - SHLVL : 1"
                                .getBytes()), message)
                .verify();
    }

    @Test
    public void decodeStructuredData() {
        final String rawStructuredData = "foo -]" + // Removal of first '[' is intentional
                                         "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"]" +
                                         "[examplePriority@32473 class=\"\\\"high\\\"\"] ";
        SyslogMessage.MessageBuilder builder = new SyslogMessage.MessageBuilder();
        SyslogMessageDecoder.decodeStructuredData(builder, Unpooled.wrappedBuffer(rawStructuredData.getBytes()));
        final SyslogMessage message = builder.build(false);

        final Map<AsciiString, Map<AsciiString, List<String>>> structuredData = message.getStructuredData();
        assertThat(structuredData.entrySet(), iterableWithSize(3));

        // Assert foo element
        final AsciiString fooElement = AsciiString.of("foo");
        assertTrue(message.hasStructuredId("foo"));
        assertThat(structuredData, hasKey(fooElement));
        assertThat(structuredData.get(fooElement).entrySet(), emptyIterable());

        // Assert exampleSDID@32473 element
        final String exampledSdId = "exampleSDID@32473";
        assertTrue(message.hasStructuredId(exampledSdId));
        final Map<AsciiString, List<String>> sdidData = message.getStructuredDataElement(exampledSdId);
        assertThat(sdidData.entrySet(), iterableWithSize(3));
        assertThat(message.getFirstStructuredValue(exampledSdId, "iut").get(), equalTo("3"));
        assertThat(sdidData, hasEntry(AsciiString.of("iut"), Collections.singletonList("3")));
        assertThat(sdidData, hasEntry(AsciiString.of("eventSource"), Collections.singletonList("Application")));
        assertThat(sdidData, hasEntry(AsciiString.of("eventID"), Collections.singletonList("1011")));

        final AsciiString examplePriorityElement = AsciiString.of("examplePriority@32473");
        assertThat(structuredData, hasKey(examplePriorityElement));
        final Map<AsciiString, List<String>> priorityData = structuredData.get(examplePriorityElement);
        assertThat(priorityData, hasEntry(AsciiString.of("class"), Collections.singletonList("\"high\"")));
    }

}
