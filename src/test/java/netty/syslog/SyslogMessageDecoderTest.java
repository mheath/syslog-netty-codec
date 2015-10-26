/*
 *   Copyright (c) 2015 Mike Heath  All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package netty.syslog;

import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.time.ZonedDateTime;

/**
 * @author Mike Heath
 */
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
				.build();
		new CodecTester()
				.decoderHanlders(new SyslogMessageDecoder())
				.expect(Unpooled.wrappedBuffer("<14>1 2014-03-20T20:14:14+00:00 loggregator 20d38e29-85bb-4833-81c8-99ba7d0c1b09 [App/0] - - SHLVL : 1".getBytes()), message)
				.verify();
	}

}
