/*
 *   Copyright (c) 2014 Intellectual Reserve, Inc.  All rights reserved.
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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.netty.util.AsciiString;

import java.time.ZonedDateTime;
import java.util.List;

import static netty.syslog.DecoderUtil.expect;
import static netty.syslog.DecoderUtil.readDigit;
import static netty.syslog.DecoderUtil.readAsciiStringToSpace;

public class SyslogMessageDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> objects) throws Exception {
		if (buffer.readableBytes() < 1) {
			return;
		}
		final SyslogMessage.MessageBuilder messageBuilder = SyslogMessage.MessageBuilder.create();

		// Decode PRI
		expect(buffer, '<');
		final int pri = readDigit(buffer);
		if (pri < 0 || pri > 191) {
			throw new DecoderException("Invalid PRIVAL " + pri);
		}
		final int facility = pri / 8;
		final int severity = pri % 8;

		messageBuilder.facility(SyslogMessage.Facility.values()[facility]);
		messageBuilder.severity(SyslogMessage.Severity.values()[severity]);

		expect(buffer, '>');

		// Decode VERSION
		if (buffer.readByte() != '1') {
			throw new DecoderException("Expected a version 1 syslog message");
		}
		expect(buffer, ' ');

		// Decode TIMESTAMP
		final ZonedDateTime timestamp;
		final AsciiString timeStampString = readAsciiStringToSpace(buffer, true);
		if (timeStampString == null) {
			timestamp = null;
		} else {
			timestamp = ZonedDateTime.parse(timeStampString);
		}
		messageBuilder.timestamp(timestamp);
		expect(buffer, ' ');

		// Decode HOSTNAME
		messageBuilder.hostname(readAsciiStringToSpace(buffer, true));
		expect(buffer, ' ');

		// Decode APP-NAME
		messageBuilder.applicationName(readAsciiStringToSpace(buffer, true));
		expect(buffer, ' ');

		// Decode PROC-ID
		messageBuilder.processId(readAsciiStringToSpace(buffer, true));
		expect(buffer, ' ');

		// Decode MSGID
		messageBuilder.messageId(readAsciiStringToSpace(buffer, true));
		expect(buffer, ' ');

		if (DecoderUtil.peek(buffer) == '-') {
			buffer.readByte();
		} else {
			expect(buffer, '[');

		}
		expect(buffer, ' ');

		final int length = buffer.readableBytes();
		messageBuilder.content(buffer.readSlice(length).retain());

		objects.add(messageBuilder.build(false));
	}

}
