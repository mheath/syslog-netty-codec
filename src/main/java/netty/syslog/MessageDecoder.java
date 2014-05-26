package netty.syslog;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;

import java.time.ZonedDateTime;
import java.util.List;

import static netty.syslog.DecoderUtil.expect;
import static netty.syslog.DecoderUtil.peek;
import static netty.syslog.DecoderUtil.readDigit;
import static netty.syslog.DecoderUtil.readStringToSpace;

/**
 * @author Mike Heath <elcapo@gmail.com>
 */
public class MessageDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> objects) throws Exception {
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

		// TODO Decode structured data
		expect(buffer, '-');
		expect(buffer, ' ');

		if (peek(buffer) != '-') {
			final int length = buffer.readableBytes();
			messageBuilder.content(buffer.slice(buffer.readerIndex(), length).retain());
			buffer.skipBytes(length);
		}

		objects.add(messageBuilder.build());
	}

}
