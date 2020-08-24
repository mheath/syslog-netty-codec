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
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.util.CharsetUtil;

class DecoderUtil {
	static int readDigit(ByteBuf buffer) {
		int digit = 0;
		while (buffer.readableBytes() > 0 && Character.isDigit(peek(buffer))) {
			digit = digit * 10 + buffer.readByte() - '0';
		}
		return digit;
	}

	static byte peek(ByteBuf buffer) {
		return buffer.getByte(buffer.readerIndex());
	}

	static void expect(ByteBuf buffer, char c) {
		byte b = buffer.readByte();
		if (b != c) {
			throw new DecoderException("Expected " + c + " at index " + buffer.readerIndex() + " but got " + (char)b);
		}
	}

	static String readStringToSpace(ByteBuf buffer, boolean checkNull) {
		return readStringToChar( buffer, ' ', checkNull );
	}

	static String readStringToChar(ByteBuf buffer, char c, boolean checkNull) {
		if (checkNull && peek(buffer) == '-') {
			buffer.readByte();
			return null;
		}
		int skipCount = -1;
		boolean escaped = false;
		ByteBuf newBuffer = Unpooled.buffer();
		for (int i = buffer.readerIndex(); i < buffer.capacity(); i++) {
			byte b = buffer.getByte(i);
			if( b == '\\' && !escaped ) {
				escaped = true;
				continue;
			} else if (b == c && !escaped) {
				skipCount = i - buffer.readerIndex();
				break;
			}
			newBuffer.writeByte( b );
			escaped = false;
		}
		if ( skipCount < 0) {
			skipCount = buffer.readableBytes();
		}
		final String s = newBuffer.toString( CharsetUtil.UTF_8);
		buffer.skipBytes( skipCount );
		return s;
	}

  static void readStructuredData(ByteBuf buffer, Message.MessageBuilder messageBuilder, boolean checkNull) {
    if (checkNull && peek(buffer) == '-') {
      buffer.readByte();
      return;
    }

    expect(buffer, '[');
    String sdid = readStringToSpace(buffer, false);
    while (peek(buffer) != ']') {
      expect(buffer, ' ');
      String key = readStringToChar(buffer, '=', false);
      expect(buffer, '=');
      expect(buffer, '"');
      String value = readStringToChar(buffer, '"', false);
      expect(buffer, '"');
      messageBuilder.addStructuredData(sdid, key, value);
    }
		expect(buffer, ']');
  }

}
