package netty.syslog;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

/**
 * @author Mike Heath <elcapo@gmail.com>
 */
class DecoderUtil {
	static int readDigit(ByteBuf buffer) {
		int digit = 0;
		while (Character.isDigit(peek(buffer))) {
			digit = digit * 10 + buffer.readByte() - '0';
		}
		return digit;
	}

	static byte peek(ByteBuf buffer) {
		return buffer.getByte(buffer.readerIndex());
	}

	static void expect(ByteBuf buffer, char c) {
		if (buffer.readByte() != c) {
			throw new DecoderException("Expected " + c + " at index " + buffer.readerIndex());
		}
	}

	static String readStringToSpace(ByteBuf buffer, boolean checkNull) {
		if (checkNull && peek(buffer) == '-') {
			buffer.readByte();
			return null;
		}
		int length = -1;
		for (int i = buffer.readerIndex(); i < buffer.capacity(); i++) {
			if (buffer.getByte(i) == ' ') {
				length = i - buffer.readerIndex();
				break;
			}
		}
		if (length < 0) {
			length = buffer.readableBytes();
		}
		return new String(buffer.readBytes(length).array());
	}
}
