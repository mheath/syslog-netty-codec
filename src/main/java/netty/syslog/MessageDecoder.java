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
import io.netty.util.CharsetUtil;

import java.time.ZonedDateTime;
import java.util.List;

import static netty.syslog.DecoderUtil.expect;
import static netty.syslog.DecoderUtil.peek;
import static netty.syslog.DecoderUtil.readDigit;
import static netty.syslog.DecoderUtil.readStringToSpace;
import static netty.syslog.DecoderUtil.skipStructuredData;

public class MessageDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> objects) throws Exception {
		if (buffer.readableBytes() < 1) {
			return;
		}
		final Message.MessageBuilder messageBuilder = Message.MessageBuilder.create();

		// Decode PRI
		expect(buffer, '<');
		final int pri = readDigit(buffer);
		if (pri < 0 || pri > 191) {
			throw new DecoderException("Invalid PRIVAL " + pri);
		}
		final int facility = pri / 8;
		final int severity = pri % 8;

		messageBuilder.facility(Message.Facility.values()[facility]);
		messageBuilder.severity(Message.Severity.values()[severity]);

		expect(buffer, '>');

		// Decode VERSION
		if (buffer.readByte() != '1') {
			throw new DecoderException("Expected a version 1 syslog message");
		}
		expect(buffer, ' ');

		// Decode TIMESTAMP
		final ZonedDateTime timestamp;
		final String timeStampString = readStringToSpace(buffer, true);
		if (timeStampString == null) {
			timestamp = null;
		} else {
			timestamp = ZonedDateTime.parse(timeStampString);
		}
		messageBuilder.timestamp(timestamp);
		expect(buffer, ' ');

		// Decode HOSTNAME
		messageBuilder.hostname(readStringToSpace(buffer, true));
		expect(buffer, ' ');

		// Decode APP-NAME
		messageBuilder.applicationName(readStringToSpace(buffer, true));
		expect(buffer, ' ');

		// Decode PROC-ID
		messageBuilder.processId(readStringToSpace(buffer, true));
		expect(buffer, ' ');

		// Decode MSGID
		messageBuilder.messageId(readStringToSpace(buffer, true));
		expect(buffer, ' ');

		// TODO Decode structured data discard for now
		skipStructuredData(buffer, true);
		expect(buffer, ' ');

		final int length = buffer.readableBytes();
		messageBuilder.content(buffer.slice(buffer.readerIndex(), length).retain());
		buffer.skipBytes(length);

		objects.add(messageBuilder.build());
	}

}
